package moze_intel.projecte.gameObjs.registries;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import moze_intel.projecte.PECore;
import moze_intel.projecte.compat.attachments.AttachmentType;
import moze_intel.projecte.compat.attachments.PEAttachments;
import moze_intel.projecte.compat.items.IItemHandler;
import moze_intel.projecte.compat.items.IItemHandlerModifiable;
import moze_intel.projecte.impl.capability.AlchBagImpl.AlchemicalBagAttachment;
import moze_intel.projecte.impl.capability.KnowledgeImpl.KnowledgeAttachment;
import net.minecraft.world.item.ItemStack;

/**
 * Fabric port: attachment types register into the PEAttachments static registry (NeoForge has a real registry + event; Fabric needs neither). Class-loading
 * this class performs registration; PECore references it during init.
 */
public class PEAttachmentTypes {

	private PEAttachmentTypes() {
	}

	public static final AttachmentType<AlchemicalBagAttachment> ALCHEMICAL_BAGS = PEAttachments.register(PECore.rl("alchemical_bags"),
			AttachmentType.builder(AlchemicalBagAttachment::new)
					.serialize(AlchemicalBagAttachment.CODEC)
					.copyHandler(AlchemicalBagAttachment::copy)
					.copyOnDeath()
					.build()
	);

	public static final AttachmentType<KnowledgeAttachment> KNOWLEDGE = PEAttachments.register(PECore.rl("knowledge"),
			AttachmentType.builder(KnowledgeAttachment::new)
					.serialize(KnowledgeAttachment.CODEC)
					.copyHandler(KnowledgeAttachment::copy)
					.copyOnDeath()
					.build()
	);

	public static final AttachmentType<Boolean> GEM_ARMOR_STATE = PEAttachments.register(PECore.rl("gem_armor_state"),
			AttachmentType.builder(holder -> false)
					.serialize(Codec.BOOL, val -> val)
					.copyHandler((attachment, holder, provider) -> attachment ? true : null)
					.copyOnDeath()
					.build()
	);

	/**
	 * Ensures the static registrations above have run.
	 */
	public static void init() {
	}

	public static <HANDLER extends IItemHandlerModifiable> HANDLER copyHandler(IItemHandler handler, Int2ObjectFunction<HANDLER> handlerCreator) {
		int slots = handler.getSlots();
		HANDLER handlerCopy = handlerCreator.get(slots);
		for (int i = 0; i < slots; i++) {
			ItemStack stack = handler.getStackInSlot(i);
			if (!stack.isEmpty()) {
				handlerCopy.setStackInSlot(i, stack.copy());
			}
		}
		return handlerCopy;
	}
}
