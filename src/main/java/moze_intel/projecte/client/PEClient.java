package moze_intel.projecte.client;

import moze_intel.projecte.PECore;
import moze_intel.projecte.events.ToolTipEvent;
import moze_intel.projecte.gameObjs.entity.EntitySWRGProjectile;
import moze_intel.projecte.gameObjs.gui.AbstractCollectorScreen;
import moze_intel.projecte.gameObjs.gui.AbstractCondenserScreen;
import moze_intel.projecte.gameObjs.gui.AlchBagScreen;
import moze_intel.projecte.gameObjs.gui.AlchChestScreen;
import moze_intel.projecte.gameObjs.gui.GUIDMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIEternalDensity;
import moze_intel.projecte.gameObjs.gui.GUIMercurialEye;
import moze_intel.projecte.gameObjs.gui.GUIRMFurnace;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK1;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK2;
import moze_intel.projecte.gameObjs.gui.GUIRelay.GUIRelayMK3;
import moze_intel.projecte.gameObjs.gui.GUITransmutation;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.sound.MovingSoundSWRG;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.rendering.ChestRenderer;
import moze_intel.projecte.rendering.EntitySpriteRenderer;
import moze_intel.projecte.rendering.LayerYue;
import moze_intel.projecte.rendering.PedestalRenderer;
import moze_intel.projecte.rendering.TransmutationRenderingOverlay;
import moze_intel.projecte.utils.ClientKeyHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class PEClient implements ClientModInitializer {

	public static final ResourceLocation ACTIVE_OVERRIDE = PECore.rl("active");
	public static final ResourceLocation MODE_OVERRIDE = PECore.rl("mode");

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void onInitializeClient() {
		ClientKeyHelper.registerKeyBindings();
		ToolTipEvent.register();
		//Install the server-to-client packet receivers collected during common init
		PacketHandler.registerClientReceivers((type, handler) ->
				ClientPlayNetworking.registerGlobalReceiver((CustomPacketPayload.Type) type,
						(payload, context) -> ((PacketHandler.IPayloadHandler) handler).handle((CustomPacketPayload) payload, context::player)));

		registerScreens();
		registerRenderers();
		addPropertyOverrides();

		//Player render layer (heart/yue circles)
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof PlayerRenderer playerRenderer) {
				registrationHelper.register(new LayerYue(playerRenderer));
			}
		});

		//Transmutation result overlay (upstream: gui layer above the crosshair)
		HudRenderCallback.EVENT.register(new TransmutationRenderingOverlay()::render);

		//SWRG projectile whoosh
		ClientEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			Minecraft mc = Minecraft.getInstance();
			if (entity instanceof EntitySWRGProjectile projectile && mc.mouseHandler.isMouseGrabbed()) {
				mc.getSoundManager().play(new MovingSoundSWRG(projectile, level.getRandom()));
			}
		});

		//TODO Fabric port: client-side /projecte dumpmissingemc command (needs porting to Fabric's client command api)
	}

	private static void registerScreens() {
		MenuScreens.register(PEContainerTypes.RM_FURNACE_CONTAINER.get(), GUIRMFurnace::new);
		MenuScreens.register(PEContainerTypes.DM_FURNACE_CONTAINER.get(), GUIDMFurnace::new);
		MenuScreens.register(PEContainerTypes.CONDENSER_CONTAINER.get(), AbstractCondenserScreen.MK1::new);
		MenuScreens.register(PEContainerTypes.CONDENSER_MK2_CONTAINER.get(), AbstractCondenserScreen.MK2::new);
		MenuScreens.register(PEContainerTypes.ALCH_CHEST_CONTAINER.get(), AlchChestScreen::new);
		MenuScreens.register(PEContainerTypes.ALCH_BAG_CONTAINER.get(), AlchBagScreen::new);
		MenuScreens.register(PEContainerTypes.ETERNAL_DENSITY_CONTAINER.get(), GUIEternalDensity::new);
		MenuScreens.register(PEContainerTypes.TRANSMUTATION_CONTAINER.get(), GUITransmutation::new);
		MenuScreens.register(PEContainerTypes.RELAY_MK1_CONTAINER.get(), GUIRelayMK1::new);
		MenuScreens.register(PEContainerTypes.RELAY_MK2_CONTAINER.get(), GUIRelayMK2::new);
		MenuScreens.register(PEContainerTypes.RELAY_MK3_CONTAINER.get(), GUIRelayMK3::new);
		MenuScreens.register(PEContainerTypes.COLLECTOR_MK1_CONTAINER.get(), AbstractCollectorScreen.MK1::new);
		MenuScreens.register(PEContainerTypes.COLLECTOR_MK2_CONTAINER.get(), AbstractCollectorScreen.MK2::new);
		MenuScreens.register(PEContainerTypes.COLLECTOR_MK3_CONTAINER.get(), AbstractCollectorScreen.MK3::new);
		MenuScreens.register(PEContainerTypes.MERCURIAL_EYE_CONTAINER.get(), GUIMercurialEye::new);
	}

	private static void registerRenderers() {
		//Block Entity
		BlockEntityRenderers.register(PEBlockEntityTypes.ALCHEMICAL_CHEST.get(), context -> new ChestRenderer(context, PECore.rl("textures/block/alchemical_chest.png"), PEBlocks.ALCHEMICAL_CHEST));
		BlockEntityRenderers.register(PEBlockEntityTypes.CONDENSER.get(), context -> new ChestRenderer(context, PECore.rl("textures/block/condenser_mk1.png"), PEBlocks.CONDENSER));
		BlockEntityRenderers.register(PEBlockEntityTypes.CONDENSER_MK2.get(), context -> new ChestRenderer(context, PECore.rl("textures/block/condenser_mk2.png"), PEBlocks.CONDENSER_MK2));
		BlockEntityRenderers.register(PEBlockEntityTypes.DARK_MATTER_PEDESTAL.get(), PedestalRenderer::new);

		//Entities
		EntityRendererRegistry.register(PEEntityTypes.WATER_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/water_orb.png")));
		EntityRendererRegistry.register(PEEntityTypes.LAVA_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lava_orb.png")));
		EntityRendererRegistry.register(PEEntityTypes.MOB_RANDOMIZER.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/randomizer.png")));
		EntityRendererRegistry.register(PEEntityTypes.LENS_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lens_explosive.png")));
		EntityRendererRegistry.register(PEEntityTypes.FIRE_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/fireball.png")));
		EntityRendererRegistry.register(PEEntityTypes.SWRG_PROJECTILE.get(), context -> new EntitySpriteRenderer<>(context, PECore.rl("textures/entity/lightning.png")));
		EntityRendererRegistry.register(PEEntityTypes.NOVA_CATALYST_PRIMED.get(), TntRenderer::new);
		EntityRendererRegistry.register(PEEntityTypes.NOVA_CATACLYSM_PRIMED.get(), TntRenderer::new);
		EntityRendererRegistry.register(PEEntityTypes.HOMING_ARROW.get(), TippableArrowRenderer::new);
	}

	@SuppressWarnings("deprecation")
	private static void addPropertyOverrides() {
		//Property Overrides
		addPropertyOverrides(ACTIVE_OVERRIDE, (stack, level, entity, seed) -> stack.getOrDefault(PEDataComponentTypes.ACTIVE.get(), false) ? 1F : 0F,
				PEItems.GEM_OF_ETERNAL_DENSITY, PEItems.VOID_RING, PEItems.ARCANA_RING, PEItems.ARCHANGEL_SMITE, PEItems.BLACK_HOLE_BAND, PEItems.BODY_STONE,
				PEItems.HARVEST_GODDESS_BAND, PEItems.IGNITION_RING, PEItems.LIFE_STONE, PEItems.MIND_STONE, PEItems.SOUL_STONE, PEItems.WATCH_OF_FLOWING_TIME,
				PEItems.ZERO_RING);
		addPropertyOverrides(MODE_OVERRIDE, (stack, level, entity, seed) ->
				stack.getOrDefault(PEDataComponentTypes.ARCANA_MODE.get(), PEItems.ARCANA_RING.asItem().getDefaultMode()).ordinal(), PEItems.ARCANA_RING);
		addPropertyOverrides(MODE_OVERRIDE, (stack, level, entity, seed) ->
				stack.getOrDefault(PEDataComponentTypes.SWRG_MODE.get(), PEItems.ARCANA_RING.asItem().getDefaultMode()).ordinal(), PEItems.SWIFTWOLF_RENDING_GALE);
	}

	@SuppressWarnings("deprecation")
	private static void addPropertyOverrides(ResourceLocation override, ItemPropertyFunction propertyGetter, ItemLike... itemProviders) {
		for (ItemLike itemProvider : itemProviders) {
			ItemProperties.register(itemProvider.asItem(), override, propertyGetter);
		}
	}
}
