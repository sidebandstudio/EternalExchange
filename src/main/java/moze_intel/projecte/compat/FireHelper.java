package moze_intel.projecte.compat;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port shim: replacements for NeoForge's BlockState#isFlammable and #onCaughtFire extensions, built on Fabric's flammable block registry.
 */
public final class FireHelper {

	private FireHelper() {
	}

	public static boolean isFlammable(BlockState state, Level level, BlockPos pos, Direction face) {
		return FlammableBlockRegistry.getDefaultInstance().get(state.getBlock()).getBurnChance() > 0;
	}

	/**
	 * NeoForge's onCaughtFire lets blocks respond to being lit; vanilla only has special-cased TNT, which is what ProjectE relies on.
	 */
	public static void onCaughtFire(BlockState state, Level level, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
		if (state.getBlock() instanceof moze_intel.projecte.gameObjs.blocks.ProjectETNT peTnt) {
			peTnt.onCaughtFire(state, level, pos, face, igniter);
			level.removeBlock(pos, false);
		} else if (state.getBlock() instanceof TntBlock) {
			if (!level.isClientSide) {
				PrimedTnt primedTnt = new PrimedTnt(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter);
				level.addFreshEntity(primedTnt);
				level.playSound(null, primedTnt.getX(), primedTnt.getY(), primedTnt.getZ(), net.minecraft.sounds.SoundEvents.TNT_PRIMED,
						net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 1.0F);
				level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
			}
			level.removeBlock(pos, false);
		}
	}
}
