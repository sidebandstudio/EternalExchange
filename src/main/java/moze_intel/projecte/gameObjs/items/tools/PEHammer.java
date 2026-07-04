package moze_intel.projecte.gameObjs.items.tools;

import moze_intel.projecte.gameObjs.IMatterType;
import moze_intel.projecte.gameObjs.PETags;
import moze_intel.projecte.gameObjs.items.IHasConditionalAttributes;
import moze_intel.projecte.utils.ToolHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import moze_intel.projecte.compat.ItemAbilities;
import moze_intel.projecte.compat.ItemAbility;
import moze_intel.projecte.compat.events.ItemAttributeModifierEvent;
import org.jetbrains.annotations.NotNull;

public class PEHammer extends PETool implements IHasConditionalAttributes, moze_intel.projecte.compat.ItemAbilityHelper.AbilityAware {

	public PEHammer(IMatterType matterType, int numCharges, Properties props) {
		super(matterType, PETags.Blocks.MINEABLE_WITH_PE_HAMMER, numCharges, props.attributes(createAttributes(matterType, 10, -3)));
	}

	@Override
	public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity damaged, @NotNull LivingEntity damager) {
		ToolHelper.attackWithCharge(stack, damaged, damager, 1.0F);
		return true;
	}

	public boolean canPerformAction(@NotNull ItemStack stack, @NotNull ItemAbility toolAction) {
		return ItemAbilities.DEFAULT_PICKAXE_ACTIONS.contains(toolAction) || ToolHelper.DEFAULT_PE_HAMMER_ACTIONS.contains(toolAction);
	}

	@Override
	public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
		return ToolHelper.canMatterMine(matterType, state.getBlock()) ? 1_200_000 : super.getDestroySpeed(stack, state);
	}

	@Override
	public void adjustAttributes(ItemAttributeModifierEvent event) {
		ToolHelper.applyChargeAttributes(event);
	}

	@NotNull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}
		return ToolHelper.digAOE(context.getLevel(), player, context.getHand(), context.getItemInHand(), context.getClickedPos(), context.getClickedFace(), true, 0);
	}
}