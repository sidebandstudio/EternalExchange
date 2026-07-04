package moze_intel.projecte.compat;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric port shim: replacements for NeoForge's ItemStack#canPerformAction and BlockState#getToolModifiedState patches.
 * <p>
 * Block responses reimplement the vanilla tool interactions (till/flatten/strip/scrape/wax-off/ignite); there is no cross-mod extension point on Fabric.
 */
public final class ItemAbilityHelper {

	private ItemAbilityHelper() {
	}

	/**
	 * Items that expose NeoForge-style abilities implement this to answer canPerformAction checks.
	 */
	public interface AbilityAware {

		boolean canPerformAction(ItemStack stack, ItemAbility ability);
	}

	public static boolean canPerformAction(ItemStack stack, ItemAbility ability) {
		if (stack.isEmpty()) {
			return false;
		}
		if (stack.getItem() instanceof AbilityAware aware) {
			return moze_intel.projecte.compat.ItemAbilityHelper.canPerformAction(moze_intel.projecte.compat.ItemAbilityHelper, aware, stack, ability);
		}
		//Vanilla defaults, mirroring NeoForge's IItemExtension defaults for the abilities ProjectE checks
		if (stack.getItem() instanceof AxeItem) {
			return ability == ItemAbilities.AXE_DIG || ability == ItemAbilities.AXE_STRIP || ability == ItemAbilities.AXE_SCRAPE || ability == ItemAbilities.AXE_WAX_OFF;
		} else if (stack.getItem() instanceof ShovelItem) {
			return ability == ItemAbilities.SHOVEL_DIG || ability == ItemAbilities.SHOVEL_FLATTEN;
		} else if (stack.getItem() instanceof HoeItem) {
			return ability == ItemAbilities.HOE_DIG || ability == ItemAbilities.HOE_TILL;
		}
		return false;
	}

	/**
	 * Mirror of NeoForge's BlockState#getToolModifiedState built on the vanilla tool-interaction data.
	 */
	@Nullable
	public static BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility ability, boolean simulate) {
		Block block = state.getBlock();
		if (ability == ItemAbilities.AXE_STRIP) {
			return AxeItem.STRIPPABLES.containsKey(block)
				   ? AxeItem.STRIPPABLES.get(block).withPropertiesOf(state)
				   : null;
		} else if (ability == ItemAbilities.AXE_SCRAPE) {
			return WeatheringCopper.getPrevious(state).orElse(null);
		} else if (ability == ItemAbilities.AXE_WAX_OFF) {
			return Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get().get(block))
					.map(unwaxed -> unwaxed.withPropertiesOf(state))
					.orElse(null);
		} else if (ability == ItemAbilities.SHOVEL_FLATTEN) {
			BlockState flattened = ShovelItem.FLATTENABLES.get(block);
			if (flattened == null) {
				return null;
			}
			BlockPos pos = context.getClickedPos();
			//Vanilla requires air above and acts on the top face
			return context.getLevel().getBlockState(pos.above()).isAir() ? flattened : null;
		} else if (ability == ItemAbilities.HOE_TILL) {
			//Vanilla tilling: dirt-like blocks with air above turn into farmland/dirt etc.
			var pair = HoeItem.TILLABLES.get(block);
			if (pair == null || !pair.getFirst().test(context)) {
				return null;
			}
			//The vanilla consumer places the state itself; the known vanilla entries all just set a block state.
			if (state.is(net.minecraft.world.level.block.Blocks.ROOTED_DIRT)) {
				return net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState();
			}
			if (state.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) || state.is(net.minecraft.world.level.block.Blocks.DIRT)
				|| state.is(net.minecraft.world.level.block.Blocks.DIRT_PATH)) {
				return net.minecraft.world.level.block.Blocks.FARMLAND.defaultBlockState();
			}
			if (state.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT)) {
				return net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState();
			}
			return null;
		} else if (ability == ItemAbilities.FIRESTARTER_LIGHT) {
			if (CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)) {
				return state.setValue(BlockStateProperties.LIT, true);
			}
			return null;
		}
		return null;
	}
}
