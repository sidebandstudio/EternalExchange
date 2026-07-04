package moze_intel.projecte.compat.attachments;

import com.mojang.serialization.Codec;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;
import moze_intel.projecte.PECore;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

/**
 * Fabric port shim: static registry and store for {@link AttachmentType}s. Values live in a map injected into Player by mixin; persistence hooks into the
 * player's save/load NBT (also reading NeoForge's {@code neoforge:attachments} tag so worlds migrated from the NeoForge version keep their data), and death
 * copying is wired to Fabric's ServerPlayerEvents.COPY_FROM in PECore.
 */
public final class PEAttachments {

	public static final String SAVED_DATA_KEY = "projecte:attachments";
	public static final String NEOFORGE_SAVED_DATA_KEY = "neoforge:attachments";

	private static final Map<ResourceLocation, AttachmentType<?>> TYPES = new LinkedHashMap<>();

	private PEAttachments() {
	}

	public static synchronized <T> AttachmentType<T> register(ResourceLocation id, AttachmentType<T> type) {
		type.bindId(id);
		if (TYPES.putIfAbsent(id, type) != null) {
			throw new IllegalArgumentException("Duplicate attachment type: " + id);
		}
		return type;
	}

	/**
	 * The mixin-provided per-entity storage.
	 */
	public interface Holder extends IAttachmentHolder {

		Map<AttachmentType<?>, Object> projecte$getAttachments();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getData(Entity entity, AttachmentType<T> type) {
		Holder holder = (Holder) entity;
		return (T) holder.projecte$getAttachments().computeIfAbsent(type, t -> type.createDefault(holder));
	}

	@SuppressWarnings("unchecked")
	public static <T> T getData(Entity entity, Supplier<AttachmentType<T>> type) {
		return getData(entity, type.get());
	}

	public static <T> void setData(Entity entity, AttachmentType<T> type, T value) {
		((Holder) entity).projecte$getAttachments().put(type, value);
	}

	public static <T> void setData(Entity entity, Supplier<AttachmentType<T>> type, T value) {
		setData(entity, type.get(), value);
	}

	public static boolean hasData(Entity entity, AttachmentType<?> type) {
		return ((Holder) entity).projecte$getAttachments().containsKey(type);
	}

	// --- persistence, called from the Player mixin ---

	@SuppressWarnings("unchecked")
	public static void save(Entity entity, CompoundTag tag) {
		Map<AttachmentType<?>, Object> attachments = ((Holder) entity).projecte$getAttachments();
		if (attachments.isEmpty()) {
			return;
		}
		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, entity.registryAccess());
		CompoundTag attachmentsTag = new CompoundTag();
		for (Map.Entry<AttachmentType<?>, Object> entry : attachments.entrySet()) {
			AttachmentType<Object> type = (AttachmentType<Object>) entry.getKey();
			Object value = entry.getValue();
			if (type.shouldSerialize(value)) {
				Codec<Object> codec = type.codec();
				codec.encodeStart(ops, value)
						.resultOrPartial(error -> PECore.LOGGER.error("Failed to save attachment {}: {}", type.id(), error))
						.ifPresent(encoded -> attachmentsTag.put(type.id().toString(), encoded));
			}
		}
		if (!attachmentsTag.isEmpty()) {
			tag.put(SAVED_DATA_KEY, attachmentsTag);
		}
	}

	public static void load(Entity entity, CompoundTag tag) {
		CompoundTag attachmentsTag;
		if (tag.contains(SAVED_DATA_KEY, Tag.TAG_COMPOUND)) {
			attachmentsTag = tag.getCompound(SAVED_DATA_KEY);
		} else if (tag.contains(NEOFORGE_SAVED_DATA_KEY, Tag.TAG_COMPOUND)) {
			//Migration path: read attachment data written by the upstream NeoForge version
			attachmentsTag = tag.getCompound(NEOFORGE_SAVED_DATA_KEY);
		} else {
			return;
		}
		Map<AttachmentType<?>, Object> attachments = ((Holder) entity).projecte$getAttachments();
		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, entity.registryAccess());
		for (Map.Entry<ResourceLocation, AttachmentType<?>> entry : TYPES.entrySet()) {
			String key = entry.getKey().toString();
			if (attachmentsTag.contains(key)) {
				AttachmentType<?> type = entry.getValue();
				type.codec()
						.parse(ops, attachmentsTag.get(key))
						.resultOrPartial(error -> PECore.LOGGER.error("Failed to load attachment {}: {}", key, error))
						.ifPresent(value -> attachments.put(type, value));
			}
		}
	}

	/**
	 * Respawn/clone copying: on a living clone (returning from the End) everything transfers; on death only copyOnDeath attachments transfer, through their
	 * copy handlers.
	 */
	@SuppressWarnings("unchecked")
	public static void copyOnRespawn(Entity oldEntity, Entity newEntity, boolean alive, HolderLookup.Provider registries) {
		Map<AttachmentType<?>, Object> oldAttachments = ((Holder) oldEntity).projecte$getAttachments();
		Map<AttachmentType<?>, Object> newAttachments = ((Holder) newEntity).projecte$getAttachments();
		for (Map.Entry<AttachmentType<?>, Object> entry : oldAttachments.entrySet()) {
			AttachmentType<Object> type = (AttachmentType<Object>) entry.getKey();
			if (alive || type.copyOnDeath()) {
				Object copy = type.copyValue(entry.getValue(), (Holder) newEntity, registries);
				if (copy != null) {
					newAttachments.put(type, copy);
				}
			}
		}
	}
}
