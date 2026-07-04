package moze_intel.projecte.mixin;

import moze_intel.projecte.events.TickEvents;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerTickMixin {

	@Inject(method = "tick", at = @At("TAIL"))
	private void projecte$onPlayerTick(CallbackInfo ci) {
		TickEvents.playerTick((Player) (Object) this);
	}
}
