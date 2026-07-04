package moze_intel.projecte.mixin;

import moze_intel.projecte.events.PlayerEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public class ItemEntityPickupMixin {

	@Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
	private void projecte$bagSuction(Player player, CallbackInfo ci) {
		if (PlayerEvents.onItemPickup((ItemEntity) (Object) this, player)) {
			ci.cancel();
		}
	}
}
