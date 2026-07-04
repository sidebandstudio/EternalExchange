package moze_intel.projecte.mixin;

import moze_intel.projecte.gameObjs.items.armor.GemLegs;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityJumpMixin {

	/**
	 * Fabric port: replaces NeoForge's LivingJumpEvent for the gem leggings' jump tracking (client-side slam timing).
	 */
	@Inject(method = "jumpFromGround", at = @At("TAIL"))
	private void projecte$onJump(CallbackInfo ci) {
		LivingEntity self = (LivingEntity) (Object) this;
		if (self instanceof Player && PEItems.GEM_LEGGINGS.get() instanceof GemLegs gemLegs) {
			gemLegs.onJump(self);
		}
	}
}
