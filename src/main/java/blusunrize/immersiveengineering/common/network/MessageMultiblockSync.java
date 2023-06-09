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
import blusunrize.immersiveengineering.api.multiblocks.TemplateMultiblock;
import blusunrize.immersiveengineering.mixin.accessors.PaletteAccess;
import blusunrize.immersiveengineering.mixin.accessors.TemplateAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.IdMapper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraftforge.network.NetworkEvent.Context;
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

	public MessageMultiblockSync(FriendlyByteBuf buf)
	{
		templates = PacketUtils.readList(buf, SyncedTemplate::new);
	}

	@Override
	public void toBytes(FriendlyByteBuf buf)
	{
		PacketUtils.writeList(buf, templates, SyncedTemplate::writeTo);
	}

	@Override
	public void process(Supplier<Context> context)
	{
		context.get().enqueueWork(() -> {
			for(SyncedTemplate synced : templates)
			{
				StructureTemplate template = new StructureTemplate();
				TemplateAccess access = (TemplateAccess)template;
				access.setSize(synced.size);
				access.getPalettes().add(synced.parts);
				TemplateMultiblock.SYNCED_CLIENT_TEMPLATES.put(synced.name, template);
			}
			ImmersiveEngineering.proxy.resetManual();
		});
	}

	public static class SyncedTemplate
	{
		private final BlockPos size;
		private final ResourceLocation name;
		private final StructureTemplate.Palette parts;

		public SyncedTemplate(StructureTemplate template, ResourceLocation name)
		{
			this.size = new BlockPos(template.getSize());
			this.parts = ((TemplateAccess)template).getPalettes().get(0);
			this.name = name;
		}

		public SyncedTemplate(FriendlyByteBuf buffer)
		{
			this.size = buffer.readBlockPos();
			this.name = buffer.readResourceLocation();
			this.parts = PaletteAccess.construct(PacketUtils.readList(buffer, SyncedTemplate::readPart));
		}

		public void writeTo(FriendlyByteBuf buffer)
		{
			buffer.writeBlockPos(size);
			buffer.writeResourceLocation(name);
			PacketUtils.writeList(buffer, parts.blocks(), SyncedTemplate::writePart);
		}

		private static StructureBlockInfo readPart(FriendlyByteBuf buffer)
		{
			IdMapper<BlockState> stateIds = GameData.getBlockStateIDMap();
			BlockState state = stateIds.byId(buffer.readVarInt());
			BlockPos pos = buffer.readBlockPos();
			CompoundTag nbt = buffer.readNbt();
			return new StructureBlockInfo(pos, Objects.requireNonNull(state), nbt);
		}

		private static void writePart(StructureBlockInfo info, FriendlyByteBuf buffer)
		{
			IdMapper<BlockState> stateIds = GameData.getBlockStateIDMap();
			buffer.writeVarInt(stateIds.getId(info.state()));
			buffer.writeBlockPos(info.pos());
			buffer.writeNbt(info.nbt());
		}
	}
}
