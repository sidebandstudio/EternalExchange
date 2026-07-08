package moze_intel.projecte;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import moze_intel.projecte.api.ProjectEAPI;
import moze_intel.projecte.api.capabilities.PECapabilities;
import moze_intel.projecte.api.nss.AbstractNSSTag;
import moze_intel.projecte.compat.ItemAbilities;
import moze_intel.projecte.compat.ItemAbilityHelper;
import moze_intel.projecte.compat.attachments.PEAttachments;
import moze_intel.projecte.compat.fml.ServerLifecycleHooks;
import moze_intel.projecte.compat.network.PacketDistributor;
import moze_intel.projecte.config.CustomEMCParser;
import moze_intel.projecte.config.ProjectEConfig;
import moze_intel.projecte.emc.EMCMappingHandler;
import moze_intel.projecte.emc.FuelMapper;
import moze_intel.projecte.events.PlayerEvents;
import moze_intel.projecte.gameObjs.registries.PEAttachmentTypes;
import moze_intel.projecte.gameObjs.registries.PEArmorMaterials;
import moze_intel.projecte.gameObjs.registries.PEBlockEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEBlockTypes;
import moze_intel.projecte.gameObjs.registries.PEBlocks;
import moze_intel.projecte.gameObjs.registries.PEContainerTypes;
import moze_intel.projecte.gameObjs.registries.PECreativeTabs;
import moze_intel.projecte.gameObjs.registries.PEDataComponentTypes;
import moze_intel.projecte.gameObjs.registries.PEEntityTypes;
import moze_intel.projecte.gameObjs.registries.PEItems;
import moze_intel.projecte.gameObjs.registries.PENormalizedSimpleStacks;
import moze_intel.projecte.gameObjs.registries.PERecipeConditions;
import moze_intel.projecte.gameObjs.registries.PERecipeSerializers;
import moze_intel.projecte.gameObjs.registries.PESoundEvents;
import moze_intel.projecte.impl.TransmutationOffline;
import moze_intel.projecte.impl.capability.AlchBagImpl;
import moze_intel.projecte.impl.capability.KnowledgeImpl;
import moze_intel.projecte.network.PacketHandler;
import moze_intel.projecte.network.ThreadCheckUUID;
import moze_intel.projecte.network.ThreadCheckUpdate;
import moze_intel.projecte.network.commands.EMCCMD;
import moze_intel.projecte.network.commands.KnowledgeCMD;
import moze_intel.projecte.network.commands.RemoveEmcCMD;
import moze_intel.projecte.network.commands.ResetEmcCMD;
import moze_intel.projecte.network.commands.SetEmcCMD;
import moze_intel.projecte.network.commands.ShowBagCMD;
import moze_intel.projecte.network.packets.to_client.SyncEmcPKT;
import moze_intel.projecte.network.packets.to_client.SyncFuelMapperPKT;
import moze_intel.projecte.network.packets.to_client.SyncWorldTransmutations;
import moze_intel.projecte.utils.WorldHelper;
import moze_intel.projecte.world_transmutation.WorldTransmutationManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.dispenser.ShearsDispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class PECore implements ModInitializer {

	public static final String MODID = ProjectEAPI.PROJECTE_MODID;
	public static final String MODNAME = "ProjectE";
	public static final GameProfile FAKEPLAYER_GAMEPROFILE = new GameProfile(UUID.fromString("590e39c7-9fb6-471b-a4c2-c0e539b2423d"), "[" + MODNAME + "]");
	public static final Logger LOGGER = LogUtils.getLogger();

	public static final List<String> uuids = new ArrayList<>();

	private static PECore instance;
	private PacketHandler packetHandler;

	public static void debugLog(String msg, Object... args) {
		if (ProjectEConfig.common.debugLogging.get()) {
			LOGGER.info(msg, args);
		} else {
			LOGGER.debug(msg, args);
		}
	}

	public static ResourceLocation rl(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

	public static PacketHandler packetHandler() {
		return instance.packetHandler;
	}

	@Override
	public void onInitialize() {
		instance = this;
		ServerLifecycleHooks.init();

		//Attachment types (Fabric port: static registry, just ensure the class initializes)
		PEAttachmentTypes.init();

		//Registries. Fabric port: unlike NeoForge's event-ordered registration, these run immediately, so dependency order matters:
		// components/sounds/armor materials before items, blocks before items and block entities, items before creative tabs.
		PEArmorMaterials.ARMOR_MATERIALS.register();
		PEDataComponentTypes.DATA_COMPONENT_TYPES.register();
		PESoundEvents.SOUND_EVENTS.register();
		PEBlockTypes.BLOCK_TYPES.register();
		PEBlocks.BLOCKS.register();
		PEItems.ITEMS.register();
		PEBlockEntityTypes.BLOCK_ENTITY_TYPES.register();
		PEContainerTypes.CONTAINER_TYPES.register();
		PEEntityTypes.ENTITY_TYPES.register();
		PECreativeTabs.CREATIVE_TABS.register();
		PENormalizedSimpleStacks.NSS_SERIALIZERS.register();
		PERecipeConditions.init();
		PERecipeSerializers.RECIPE_SERIALIZERS.register();

		//Fabric port: upstream ships furnace fuel times as a NeoForge data map (data/neoforge/data_maps/item/furnace_fuels.json),
		// which Fabric does not read; register the same values with Fabric's fuel registry (feeds vanilla furnaces, the
		// matter furnaces, and the fuel slot filters alike)
		net.fabricmc.fabric.api.registry.FuelRegistry.INSTANCE.add(PEItems.ALCHEMICAL_COAL, 6_400);
		net.fabricmc.fabric.api.registry.FuelRegistry.INSTANCE.add(PEItems.MOBIUS_FUEL, 25_600);
		net.fabricmc.fabric.api.registry.FuelRegistry.INSTANCE.add(PEItems.AETERNALIS_FUEL, 102_400);
		net.fabricmc.fabric.api.registry.FuelRegistry.INSTANCE.add(PEBlocks.ALCHEMICAL_COAL, 57_600);
		net.fabricmc.fabric.api.registry.FuelRegistry.INSTANCE.add(PEBlocks.MOBIUS_FUEL, 230_400);
		net.fabricmc.fabric.api.registry.FuelRegistry.INSTANCE.add(PEBlocks.AETERNALIS_FUEL, 921_600);

		//Entity capabilities: knowledge + alchemical bags live on players
		PECapabilities.ALCH_BAG_CAPABILITY.registerForTypes((entity, context) -> new AlchBagImpl((Player) entity), EntityType.PLAYER);
		PECapabilities.KNOWLEDGE_CAPABILITY.registerForTypes((entity, context) -> new KnowledgeImpl((Player) entity), EntityType.PLAYER);

		//Config files (Fabric port: loads eagerly)
		ProjectEConfig.register();

		//Packets
		this.packetHandler = new PacketHandler();

		//Player events + attachment respawn copying
		PlayerEvents.register();
		ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) ->
				PEAttachments.copyOnRespawn(oldPlayer, newPlayer, alive, newPlayer.registryAccess()));

		//Commands
		CommandRegistrationCallback.EVENT.register((dispatcher, context, environment) -> registerCommands(dispatcher, context));

		//Fabric port: replaces NeoForge's LeftClickBlock/onLeftClickEntity hooks for the Archangel Smite volley
		net.fabricmc.fabric.api.event.player.AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
			ItemStack held = player.getItemInHand(hand);
			if (!level.isClientSide && !held.isEmpty() && held.getItem() instanceof moze_intel.projecte.gameObjs.items.rings.ArchangelSmite smite) {
				smite.leftClickBlock(player, held);
			}
			return net.minecraft.world.InteractionResult.PASS;
		});
		net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
			ItemStack held = player.getItemInHand(hand);
			if (!held.isEmpty() && held.getItem() instanceof moze_intel.projecte.gameObjs.items.rings.ArchangelSmite smite) {
				smite.onLeftClickEntity(held, player, entity);
			}
			return net.minecraft.world.InteractionResult.PASS;
		});

		//Fabric port: replaces NeoForge's onItemUseFirst hook for the philosopher's stone (must run before block interactions)
		net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
			ItemStack stack = player.getItemInHand(hand);
			if (!stack.isEmpty() && stack.getItem() instanceof moze_intel.projecte.gameObjs.items.PhilosophersStone stone) {
				return stone.onItemUseFirst(stack, new UseOnContext(player, hand, hitResult));
			}
			return net.minecraft.world.InteractionResult.PASS;
		});

		//World transmutation reload listener
		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(new IdentifiableResourceReloadListener() {
			@Override
			public ResourceLocation getFabricId() {
				return rl("world_transmutations");
			}

			@Override
			@NotNull
			public java.util.concurrent.CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager,
					net.minecraft.util.profiling.ProfilerFiller preparationsProfiler, net.minecraft.util.profiling.ProfilerFiller reloadProfiler,
					java.util.concurrent.Executor backgroundExecutor, java.util.concurrent.Executor gameExecutor) {
				return WorldTransmutationManager.INSTANCE.reload(barrier, manager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
			}
		});

		//EMC (re)mapping: on server start and after /reload (Fabric port: upstream captured a reload listener + deferred to the datapack sync event)
		ServerLifecycleEvents.SERVER_STARTED.register(server -> remapEmc(server));
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, success) -> {
			if (success) {
				remapEmc(server);
				for (ServerPlayer player : server.getPlayerList().getPlayers()) {
					syncDataToPlayer(player);
				}
			}
		});

		//Sync EMC/fuel/world transmutation data to newly joining players
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> syncDataToPlayer(handler.player));

		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			if (!ThreadCheckUUID.hasRunServer()) {
				new ThreadCheckUUID(true).start();
			}
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(this::serverQuit);

		new ThreadCheckUpdate().start();
		EMCMappingHandler.loadMappers();
		commonSetup();
	}

	private static void remapEmc(MinecraftServer server) {
		long start = System.currentTimeMillis();
		//Clear the cached created tags
		AbstractNSSTag.clearCreatedTags();
		CustomEMCParser.init(server.registryAccess());
		try {
			EMCMappingHandler.map(server.resources.managers(), server.registryAccess(), server.getResourceManager());
			LOGGER.info("Registered {} EMC values. (took {} ms)", EMCMappingHandler.getEmcMapSize(), System.currentTimeMillis() - start);
		} catch (Throwable t) {
			LOGGER.error("Error calculating EMC values", t);
		}
	}

	private static void syncDataToPlayer(ServerPlayer player) {
		//Fabric port: upstream skipped memory connections (integrated-server host shares the server maps)
		if (player.server.isSingleplayerOwner(player.getGameProfile())) {
			return;
		}
		PacketDistributor.sendToPlayer(player, SyncEmcPKT.serializeEmcData(player.registryAccess()), FuelMapper.getSyncPacket());
		PacketDistributor.sendToPlayer(player, WorldTransmutationManager.getSyncPacket());
	}

	private void commonSetup() {
		//Dispenser Behavior
		registerDispenseBehavior(new ShearsDispenseItemBehavior(), PEItems.DARK_MATTER_SHEARS, PEItems.RED_MATTER_SHEARS, PEItems.RED_MATTER_KATAR);
		DispenserBlock.registerBehavior(PEBlocks.NOVA_CATALYST, PEBlocks.NOVA_CATALYST.getBlock().createDispenseItemBehavior());
		DispenserBlock.registerBehavior(PEBlocks.NOVA_CATACLYSM, PEBlocks.NOVA_CATACLYSM.getBlock().createDispenseItemBehavior());
		registerDispenseBehavior(new OptionalDispenseItemBehavior() {
			@NotNull
			@Override
			protected ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
				//[VanillaCopy] Based off the flint and steel dispense behavior
				if (!ItemAbilityHelper.canPerformAction(stack, ItemAbilities.FIRESTARTER_LIGHT)) {
					//Only allow using the arcana ring to ignite things when on ignition mode
					setSuccess(false);
					return super.execute(source, stack);
				}
				Level level = source.level();
				setSuccess(true);
				Direction direction = source.state().getValue(DispenserBlock.FACING);
				BlockPos pos = source.pos().relative(direction);
				BlockState state = level.getBlockState(pos);
				if (BaseFireBlock.canBePlacedAt(level, pos, direction)) {
					level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
					level.gameEvent(null, GameEvent.BLOCK_PLACE, pos);
				} else {
					Direction opposite = direction.getOpposite();
					BlockHitResult hitResult = new BlockHitResult(pos.getCenter(), opposite, pos, false);
					UseOnContext context = new UseOnContext(level, null, InteractionHand.MAIN_HAND, stack, hitResult);
					BlockState modifiedState = ItemAbilityHelper.getToolModifiedState(state, context, ItemAbilities.FIRESTARTER_LIGHT, false);
					if (modifiedState != null) {
						level.setBlockAndUpdate(pos, modifiedState);
						level.gameEvent(null, GameEvent.BLOCK_CHANGE, pos);
					} else if (moze_intel.projecte.compat.FireHelper.isFlammable(state, level, pos, opposite)) {
						moze_intel.projecte.compat.FireHelper.onCaughtFire(state, level, pos, opposite, null);
						if (state.getBlock() instanceof TntBlock) {
							level.removeBlock(pos, false);
						}
					} else {
						setSuccess(false);
					}
				}
				return stack;
			}
		}, PEItems.IGNITION_RING, PEItems.ARCANA_RING);
		DispenserBlock.registerBehavior(PEItems.EVERTIDE_AMULET, new DefaultDispenseItemBehavior() {
			@NotNull
			@Override
			public ItemStack execute(@NotNull BlockSource source, @NotNull ItemStack stack) {
				//Based off of vanilla's bucket dispense behaviors
				// Note: We only do evertide, not volcanite, as placing lava requires EMC
				//Fabric port: no fluid-handler block capability to fill; always place into the world
				Level level = source.level();
				Direction direction = source.state().getValue(DispenserBlock.FACING);
				BlockPos pos = source.pos().relative(direction);
				WorldHelper.placeFluid(null, level, pos, Fluids.WATER, !ProjectEConfig.server.items.opEvertide.get());
				level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), PESoundEvents.WATER_MAGIC.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
				return stack;
			}
		});
	}

	private static void registerDispenseBehavior(DispenseItemBehavior behavior, ItemLike... items) {
		for (ItemLike item : items) {
			DispenserBlock.registerBehavior(item, behavior);
		}
	}

	private void registerCommands(com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack> dispatcher, CommandBuildContext context) {
		dispatcher.register(Commands.literal("projecte")
				.requires(PEPermissions.COMMAND)
				.then(RemoveEmcCMD.register(context))
				.then(ResetEmcCMD.register(context))
				.then(SetEmcCMD.register(context))
				.then(ShowBagCMD.register(context))
				.then(EMCCMD.register(context))
				.then(KnowledgeCMD.register(context))
		);
	}

	private void serverQuit(MinecraftServer server) {
		//Ensure we save any changes to the custom emc file
		CustomEMCParser.flush(server.registryAccess());
		TransmutationOffline.cleanAll();
		EMCMappingHandler.clearEmcMap();
	}
}
