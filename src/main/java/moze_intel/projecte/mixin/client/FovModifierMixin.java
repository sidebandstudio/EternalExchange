package moze_intel.projecte.mixin.client;

import moze_intel.projecte.events.PlayerRender;
import net.minecraft.client.player.AbstractClientPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class FovModifierMixin {

	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
	private void projecte$gemBootsFov(CallbackInfoReturnable<Float> cir) {
		float adjusted = PlayerRender.adjustFovModifier((AbstractClientPlayer) (Object) this, cir.getReturnValueF());
		if (adjusted != cir.getReturnValueF()) {
			cir.setReturnValue(adjusted);
		}
	}
}
