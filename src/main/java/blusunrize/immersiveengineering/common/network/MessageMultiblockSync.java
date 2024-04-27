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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.Palette;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record MessageMultiblockSync(List<SyncedTemplate> templates) implements IMessage
{
	public static final Type<MessageMultiblockSync> ID = IMessage.createType("multiblock_sync");
	public static final StreamCodec<RegistryFriendlyByteBuf, MessageMultiblockSync> CODEC = SyncedTemplate.CODEC
			.apply(ByteBufCodecs.list())
			.map(MessageMultiblockSync::new, MessageMultiblockSync::templates);

	@Override
	public void process(IPayloadContext context)
	{
		context.enqueueWork(() -> {
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

	public record SyncedTemplate(BlockPos size, ResourceLocation name, StructureTemplate.Palette parts)
	{
		private static final StreamCodec<RegistryFriendlyByteBuf, StructureBlockInfo> BLOCK_CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, StructureBlockInfo::pos,
				ByteBufCodecs.idMapper(Block.BLOCK_STATE_REGISTRY), StructureBlockInfo::state,
				ByteBufCodecs.COMPOUND_TAG, StructureBlockInfo::nbt,
				StructureBlockInfo::new
		);
		private static final StreamCodec<RegistryFriendlyByteBuf, StructureTemplate.Palette> PALETTE_CODEC = BLOCK_CODEC
				.apply(ByteBufCodecs.list())
				.map(PaletteAccess::construct, Palette::blocks);
		public static final StreamCodec<RegistryFriendlyByteBuf, SyncedTemplate> CODEC = StreamCodec.composite(
				BlockPos.STREAM_CODEC, SyncedTemplate::size,
				ResourceLocation.STREAM_CODEC, SyncedTemplate::name,
				PALETTE_CODEC, SyncedTemplate::parts,
				SyncedTemplate::new
		);


		public SyncedTemplate(StructureTemplate template, ResourceLocation name)
		{
			this(new BlockPos(template.getSize()), name, ((TemplateAccess)template).getPalettes().get(0));
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type()
	{
		return ID;
	}
}
