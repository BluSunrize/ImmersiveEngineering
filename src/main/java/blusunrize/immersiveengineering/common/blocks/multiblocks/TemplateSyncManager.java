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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID)
public class TemplateSyncManager
{
	@SubscribeEvent
	public static void onLogin(PlayerLoggedInEvent ev)
	{
		// TODO run when StructureTemplateManager#onResourceManagerReload is called and send to all players then
		if(!(ev.getEntity().level() instanceof ServerLevel level)||!(ev.getEntity() instanceof ServerPlayer player))
			return;
		List<MessageMultiblockSync.SyncedTemplate> toSync = new ArrayList<>();
		for(IMultiblock mb : MultiblockHandler.getMultiblocks())
			if(mb instanceof TemplateMultiblock templateMB)
			{
				StructureTemplate template = templateMB.getTemplate(level).template();
				ResourceLocation rl = templateMB.getTemplateLocation();
				toSync.add(new SyncedTemplate(template, rl));
			}
		ImmersiveEngineering.packetHandler.send(
				PacketDistributor.PLAYER.with(() -> player), new MessageMultiblockSync(toSync)
		);
	}
}