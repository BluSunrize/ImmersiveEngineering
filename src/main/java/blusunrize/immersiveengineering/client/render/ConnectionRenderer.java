/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.WireRange;
import blusunrize.immersiveengineering.mixin.accessors.client.CompiledChunkAccess;
import blusunrize.immersiveengineering.mixin.accessors.client.RenderChunkAccess;
import com.mojang.blaze3d.vertex.BufferBuilder;
import malte0811.modelsplitter.model.UVCoords;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ChunkBufferBuilderPack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.CompiledChunk;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher.RenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/*
TODO:
 - Redraw chunks a wire passes through when the wire is added/removed
 - Remove wires from section data structure
 - Sync wires early in case a "middle" chunk is synced first
 - Special treatment vertical wires
 - Cache data needed to render a segment, to make connection rendering near alloc-free
 - Delete SmartLightingQuad and related code!
 - Write a comment for Forge about threading, maybe
 */
public class ConnectionRenderer
{
	public static void renderConnectionsInSection(
			CompiledChunk compiled, ChunkBufferBuilderPack buffers,
			@Nullable RenderChunkRegion region, RenderChunk renderChunk
	)
	{
		if(region==null)
			return;
		BlockPos chunkOrigin = renderChunk.getOrigin();
		SectionPos section = SectionPos.of(chunkOrigin);
		GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(Minecraft.getInstance().level);
		List<WireRange> connectionParts = globalNet.getCollisionData().getWiresIn(section);
		if(connectionParts==null||connectionParts.isEmpty())
			return;
		RenderType renderType = RenderType.solid();
		BufferBuilder builder = buffers.builder(renderType);
		var compiledAccess = (CompiledChunkAccess)compiled;
		if(compiledAccess.getHasLayer().add(renderType))
		{
			((RenderChunkAccess)renderChunk).invokeBeginLayer(builder);
		}
		for(var connection : connectionParts)
		{
			ConnectionPoint connectionOrigin = connection.connection().getEndA();
			renderConnection(builder, connection, connectionOrigin.getPosition().subtract(chunkOrigin), region);
		}
		compiledAccess.setIsCompletelyEmpty(false);
		compiledAccess.getHasBlocks().add(renderType);
	}

	public static void renderConnection(BufferBuilder out, WireRange toRender, Vec3i offset, BlockAndTintGetter level)
	{
		CatenaryData catenaryData = toRender.connection().getCatenaryData();
		Vec3 lastPoint = catenaryData.getRenderPoint(toRender.firstPointToRender());
		int lastLight = getLight(toRender.connection(), lastPoint, level);
		for(int startPoint = toRender.firstPointToRender(); startPoint < toRender.lastPointToRender(); ++startPoint)
		{
			Vec3 nextPoint = catenaryData.getRenderPoint(startPoint+1);
			int nextLight = getLight(toRender.connection(), nextPoint, level);
			renderSegment(toRender.connection(), lastLight, nextLight, lastPoint, nextPoint, out, offset);
			lastPoint = nextPoint;
			lastLight = nextLight;
		}
	}

	private static void renderSegment(
			Connection connection,
			int lightStart, int lightEnd,
			Vec3 start, Vec3 end,
			BufferBuilder out, Vec3i offset
	)
	{
		CatenaryData catenaryData = connection.getCatenaryData();
		double radius = connection.type.getRenderDiameter()/2;
		int color = connection.type.getColour(connection);
		Vec3 horizontalUnscaledNormal = new Vec3(catenaryData.delta().z, 0, -catenaryData.delta().x);
		Vec3 horNormal = horizontalUnscaledNormal.scale(radius/catenaryData.horLength());
		renderBidirectionalQuad(out, start, end, horNormal, color, lightStart, lightEnd, offset);
		//TODO radius vector based on connection slope!
		renderBidirectionalQuad(out, start, end, new Vec3(0, radius, 0), color, lightStart, lightEnd, offset);
	}

	private static int getLight(Connection connection, Vec3 point, BlockAndTintGetter level)
	{
		BlockPos posToCheck = connection.getEndA().getPosition().offset(point.x, point.y, point.z);
		return LevelRenderer.getLightColor(level, posToCheck);
	}

	//TODO move somewhere else
	private static int getByte(int value, int lowestBit)
	{
		return (value >> lowestBit)&255;
	}

	private static void renderBidirectionalQuad(
			BufferBuilder out, Vec3 start, Vec3 end, Vec3 radius, int color, int lightStart, int lightEnd, Vec3i offset
	)
	{
		//TODO cache FFS!
		TextureAtlasSprite texture = Minecraft.getInstance().getModelManager()
				.getAtlas(InventoryMenu.BLOCK_ATLAS)
				.getSprite(ImmersiveEngineering.rl("block/wire"));
		UVCoords[] uvs = {
				new UVCoords(texture.getU0(), texture.getV0()),
				new UVCoords(texture.getU1(), texture.getV0()),
				new UVCoords(texture.getU1(), texture.getV1()),
				new UVCoords(texture.getU0(), texture.getV1()),
		};

		Vec3[] vertices = {
				//TODO reduce allocs? Or cache "something"?
				start.add(radius), end.add(radius), end.subtract(radius), start.subtract(radius),
		};
		for(int i = 0; i < vertices.length; i++)
			vertex(out, vertices[i], uvs[i], color, i==0||i==3?lightStart: lightEnd, offset);
		for(int i = vertices.length-1; i >= 0; i--)
			vertex(out, vertices[i], uvs[i], color, i==0||i==3?lightStart: lightEnd, offset);
	}

	private static void vertex(BufferBuilder out, Vec3 point, UVCoords uv, int color, int light, Vec3i offset)
	{
		out.vertex(
				(float)(point.x+offset.getX()), (float)(point.y+offset.getY()), (float)(point.z+offset.getZ()),
				getByte(color, 0)/255f, getByte(color, 8)/255f, getByte(color, 16)/255f, 1,
				(float)uv.u(), (float)uv.v(),
				OverlayTexture.NO_OVERLAY,
				light,
				//TODO
				0, 1, 0
		);
	}
}
