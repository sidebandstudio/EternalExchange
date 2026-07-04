package moze_intel.projecte.mixin;

import java.util.IdentityHashMap;
import java.util.Map;
import moze_intel.projecte.compat.attachments.AttachmentType;
import moze_intel.projecte.compat.attachments.PEAttachments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerAttachmentsMixin implements PEAttachments.Holder {

	@Unique
	private final Map<AttachmentType<?>, Object> projecte$attachments = new IdentityHashMap<>();

	@Override
	public Map<AttachmentType<?>, Object> projecte$getAttachments() {
		return projecte$attachments;
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void projecte$saveAttachments(CompoundTag tag, CallbackInfo ci) {
		PEAttachments.save((Player) (Object) this, tag);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void projecte$loadAttachments(CompoundTag tag, CallbackInfo ci) {
		PEAttachments.load((Player) (Object) this, tag);
	}
}
