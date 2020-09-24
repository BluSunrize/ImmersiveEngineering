/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.network;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.StaticTemplateManager;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.registries.GameData;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class MessageMultiblockSync implements IMessage
{
	private final List<SyncedTemplate> templates;

	public MessageMultiblockSync(List<SyncedTemplate> templatesToSync)
	{
		this.templates = templatesToSync;
	}

	public MessageMultiblockSync(PacketBuffer buf)
	{
		templates = PacketUtils.readList(buf, SyncedTemplate::new);
	}

	@Override
	public void toBytes(PacketBuffer buf)
	{
		PacketUtils.writeList(buf, templates, SyncedTemplate::writeTo);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			for(SyncedTemplate synced : templates)
			{
				Template template = new Template();
				template.size = synced.size;
				template.blocks.add(synced.parts);
				StaticTemplateManager.SYNCED_CLIENT_TEMPLATES.put(synced.name, template);
			}
			for(IMultiblock mb : MultiblockHandler.getMultiblocks())
				if(mb instanceof TemplateMultiblock)
					((TemplateMultiblock)mb).reset();
			ImmersiveEngineering.proxy.resetManual();
		});
	}

	public static class SyncedTemplate
	{
		private final BlockPos size;
		private final ResourceLocation name;
		private final Template.Palette parts;

		public SyncedTemplate(Template template, ResourceLocation name)
		{
			this.size = template.getSize();
			this.parts = template.blocks.get(0);
			this.name = name;
		}

		public SyncedTemplate(PacketBuffer buffer)
		{
			this.size = buffer.readBlockPos();
			this.name = buffer.readResourceLocation();
			this.parts = new Template.Palette(PacketUtils.readList(buffer, SyncedTemplate::readPart));
		}

		public void writeTo(PacketBuffer buffer)
		{
			buffer.writeBlockPos(size);
			buffer.writeResourceLocation(name);
			PacketUtils.writeList(buffer, parts.func_237157_a_(), SyncedTemplate::writePart);
		}

		private static BlockInfo readPart(PacketBuffer buffer)
		{
			ObjectIntIdentityMap<BlockState> stateIds = GameData.getBlockStateIDMap();
			BlockState state = stateIds.getByValue(buffer.readVarInt());
			BlockPos pos = buffer.readBlockPos();
			CompoundNBT nbt = buffer.readCompoundTag();
			return new BlockInfo(pos, Objects.requireNonNull(state), nbt);
		}

		private static void writePart(BlockInfo info, PacketBuffer buffer)
		{
			ObjectIntIdentityMap<BlockState> stateIds = GameData.getBlockStateIDMap();
			buffer.writeVarInt(stateIds.get(info.state));
			buffer.writeBlockPos(info.pos);
			buffer.writeCompoundTag(info.nbt);
		}
	}
}
