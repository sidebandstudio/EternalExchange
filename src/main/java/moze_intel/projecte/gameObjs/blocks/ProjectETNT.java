package moze_intel.projecte.gameObjs.blocks;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProjectETNT extends TntBlock {

	private final TNTEntityCreator tntEntityCreator;

	public ProjectETNT(Properties properties, TNTEntityCreator tntEntityCreator) {
		super(properties);
		this.tntEntityCreator = tntEntityCreator;
		//Fabric port: NeoForge's getFlammability override becomes a flammable-registry entry
		FlammableBlockRegistry.getDefaultInstance().add(this, 15, 100);
	}

	/**
	 * Fabric port: NeoForge routes every vanilla ignition path through this overridable hook; on Fabric the vanilla entry points below are overridden to call it
	 * instead of vanilla's static explode (which would spawn a plain TNT entity).
	 */
	public void onCaughtFire(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @Nullable net.minecraft.core.Direction side, @Nullable LivingEntity igniter) {
		if (!level.isClientSide) {
			createAndAddEntity(level, pos, igniter);
			level.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
		}
	}

	@Override
	protected void onPlace(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull BlockState oldState, boolean movedByPiston) {
		if (!oldState.is(state.getBlock())) {
			if (level.hasNeighborSignal(pos)) {
				onCaughtFire(state, level, pos, null, null);
				level.removeBlock(pos, false);
			}
		}
	}

	@Override
	protected void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull net.minecraft.world.level.block.Block neighborBlock,
			@NotNull BlockPos neighborPos, boolean movedByPiston) {
		if (level.hasNeighborSignal(pos)) {
			onCaughtFire(state, level, pos, null, null);
			level.removeBlock(pos, false);
		}
	}

	@NotNull
	@Override
	protected ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
			@NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
		if (!stack.is(Items.FLINT_AND_STEEL) && !stack.is(Items.FIRE_CHARGE)) {
			return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
		}
		onCaughtFire(state, level, pos, hitResult.getDirection(), player);
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
		net.minecraft.world.item.Item item = stack.getItem();
		if (stack.is(Items.FLINT_AND_STEEL)) {
			stack.hurtAndBreak(1, player, net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));
		} else {
			stack.consume(1, player);
		}
		player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(item));
		return ItemInteractionResult.sidedSuccess(level.isClientSide);
	}

	@Override
	protected void onProjectileHit(@NotNull Level level, @NotNull BlockState state, @NotNull BlockHitResult hit, @NotNull Projectile projectile) {
		if (!level.isClientSide) {
			BlockPos pos = hit.getBlockPos();
			net.minecraft.world.entity.Entity owner = projectile.getOwner();
			if (projectile.isOnFire() && projectile.mayInteract(level, pos)) {
				onCaughtFire(state, level, pos, null, owner instanceof LivingEntity living ? living : null);
				level.removeBlock(pos, false);
			}
		}
	}

	public void createAndAddEntity(@NotNull Level level, @NotNull BlockPos pos, @Nullable LivingEntity igniter) {
		PrimedTnt tnt = tntEntityCreator.create(level, pos.getX() + 0.5F, pos.getY(), pos.getZ() + 0.5F, igniter);
		level.addFreshEntity(tnt);
		level.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
	}

	public DispenseItemBehavior createDispenseItemBehavior() {
		//[VanillaCopy] Based off vanilla's TNT behavior
		return new DefaultDispenseItemBehavior() {
			@NotNull
			@Override
			protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
				BlockPos blockpos = source.pos().relative(source.state().getValue(DispenserBlock.FACING));
				createAndAddEntity(source.level(), blockpos, null);
				source.level().gameEvent(null, GameEvent.ENTITY_PLACE, blockpos);
				stack.shrink(1);
				return stack;
			}
		};
	}

	@Override
	public void wasExploded(Level level, @NotNull BlockPos pos, @NotNull Explosion explosion) {
		if (!level.isClientSide) {
			PrimedTnt tnt = tntEntityCreator.create(level, (float) pos.getX() + 0.5F, pos.getY(), (float) pos.getZ() + 0.5F, explosion.getIndirectSourceEntity());
			int fuse = tnt.getFuse();
			tnt.setFuse((short) (level.random.nextInt(fuse / 4) + fuse / 8));
			level.addFreshEntity(tnt);
		}
	}

	@FunctionalInterface
	public interface TNTEntityCreator {

		PrimedTnt create(Level level, double posX, double posY, double posZ, @Nullable LivingEntity igniter);
	}
}