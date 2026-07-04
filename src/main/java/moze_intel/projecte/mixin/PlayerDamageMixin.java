package moze_intel.projecte.mixin;

import moze_intel.projecte.events.PlayerEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public class PlayerDamageMixin {

	@ModifyVariable(method = "actuallyHurt", at = @At("HEAD"), ordinal = 0, argsOnly = true)
	private float projecte$reduceDamage(float amount, DamageSource source) {
		return PlayerEvents.modifyIncomingDamage((Player) (Object) this, source, amount);
	}
}
