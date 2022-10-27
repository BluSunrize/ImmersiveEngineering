/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.common.network.MessageMultiblockSync;
import blusunrize.immersiveengineering.common.network.MessageMultiblockSync.SyncedTemplate;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.PacketDistributor.PacketTarget;
import net.minecraftforge.resource.PathPackResources;
import net.minecraftforge.resource.ResourcePackLoader;
import net.minecraftforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID)
public class StaticTemplateManager
{
	public static ExistingFileHelper EXISTING_HELPER;
	public static Map<ResourceLocation, StructureTemplate> SYNCED_CLIENT_TEMPLATES = new HashMap<>();

	private static Optional<InputStream> getModResource(PackType type, ResourceLocation name, @Nullable MinecraftServer server)
	{
		if(EXISTING_HELPER!=null)
		{
			try
			{
				int slash = name.getPath().indexOf('/');
				String prefix = name.getPath().substring(0, slash);
				ResourceLocation shortLoc = new ResourceLocation(
						name.getNamespace(),
						name.getPath().substring(slash+1)
				);
				return Optional.of(EXISTING_HELPER.getResource(shortLoc, type, "", prefix).open());
			} catch(Exception x)
			{
				throw new RuntimeException(x);
			}
		}
		else if(server!=null)
		{
			try
			{
				Optional<Resource> resource = server.getResourceManager().getResource(name);
				if (resource.isPresent())
					return Optional.of(resource.get().open());
				else
					return Optional.empty();
			} catch(IOException x)
			{
				return Optional.empty();
			}
		}
		else
		{
			IELogger.error("Falling back to mod resource packs for resource {}", name);
			return ModList.get().getMods().stream()
					.map(IModInfo::getModId)
					.map(ResourcePackLoader::getPackFor)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.filter(mfrp -> mfrp.hasResource(type, name))
					.map(mfrp -> getInputStreamOrThrow(type, name, mfrp))
					.findAny();
		}
	}

	private static InputStream getInputStreamOrThrow(PackType type, ResourceLocation name, PathPackResources source)
	{
		try
		{
			return source.getResource(type, name);
		} catch(IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static StructureTemplate loadStaticTemplate(ResourceLocation loc, @Nullable MinecraftServer server) throws IOException
	{
		if(server==null&&EXISTING_HELPER==null)
			return Objects.requireNonNull(SYNCED_CLIENT_TEMPLATES.get(loc));
		else
		{
			String path = "structures/"+loc.getPath()+".nbt";
			Optional<InputStream> optStream = getModResource(PackType.SERVER_DATA,
					new ResourceLocation(loc.getNamespace(), path), server);
			if(optStream.isPresent())
				return loadTemplate(optStream.get());
			throw new RuntimeException("Mod resource not found: "+loc);
		}
	}

	private static StructureTemplate loadTemplate(InputStream inputStreamIn) throws IOException
	{
		CompoundTag compoundnbt = NbtIo.readCompressed(inputStreamIn);
		StructureTemplate template = new StructureTemplate();
		template.load(compoundnbt);
		return template;
	}

	public static void syncMultiblockTemplates(PacketTarget target, boolean resetMBs)
	{
		List<MessageMultiblockSync.SyncedTemplate> toSync = new ArrayList<>();
		for(IMultiblock mb : MultiblockHandler.getMultiblocks())
			if(mb instanceof TemplateMultiblock templateMB)
			{
				if(resetMBs)
					templateMB.reset();
				StructureTemplate template = templateMB.getTemplate(ServerLifecycleHooks.getCurrentServer());
				ResourceLocation rl = templateMB.getTemplateLocation();
				toSync.add(new SyncedTemplate(template, rl));
			}
		ImmersiveEngineering.packetHandler.send(target, new MessageMultiblockSync(toSync));
	}

	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent ev)
	{
		syncMultiblockTemplates(PacketDistributor.PLAYER.with(() -> (ServerPlayer)ev.getEntity()), false);
	}
}