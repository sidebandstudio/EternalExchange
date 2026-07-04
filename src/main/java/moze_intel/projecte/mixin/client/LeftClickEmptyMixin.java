package moze_intel.projecte.mixin.client;

import moze_intel.projecte.PECore;
import moze_intel.projecte.gameObjs.registries.PEItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.jetbrains.annotations.Nullable;

@Mixin(Minecraft.class)
public class LeftClickEmptyMixin {

	@Shadow
	@Nullable
	public net.minecraft.client.player.LocalPlayer player;

	@Shadow
	@Nullable
	public HitResult hitResult;

	/**
	 * Fabric port: replaces NeoForge's PlayerInteractEvent.LeftClickEmpty for the Archangel Smite volley (server re-validates the held item).
	 */
	@Inject(method = "startAttack", at = @At("HEAD"))
	private void projecte$leftClickEmpty(CallbackInfoReturnable<Boolean> cir) {
		if (player != null && hitResult != null && hitResult.getType() == HitResult.Type.MISS && player.getMainHandItem().is(PEItems.ARCHANGEL_SMITE)) {
			PECore.packetHandler().activateArchangel();
		}
	}
}
