package moze_intel.projecte.mixin;

import java.util.List;
import moze_intel.projecte.gameObjs.items.tools.PEKatar;
import moze_intel.projecte.gameObjs.items.tools.PESword;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public class PlayerSweepAttackMixin {

	/**
	 * Fabric port: NeoForge routes the sweep-attack area through Item#getSweepHitBox; this widens the vanilla box for the katar/swords the same way.
	 */
	@Redirect(method = "attack", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;"))
	private List<LivingEntity> projecte$sweepHitBox(Level level, Class<LivingEntity> clazz, AABB original, Entity target) {
		Player self = (Player) (Object) this;
		ItemStack held = self.getMainHandItem();
		if (held.getItem() instanceof PEKatar katar) {
			return level.getEntitiesOfClass(clazz, katar.getSweepHitBox(held, self, target));
		} else if (held.getItem() instanceof PESword sword) {
			return level.getEntitiesOfClass(clazz, sword.getSweepHitBox(held, self, target));
		}
		return level.getEntitiesOfClass(clazz, original);
	}
}
