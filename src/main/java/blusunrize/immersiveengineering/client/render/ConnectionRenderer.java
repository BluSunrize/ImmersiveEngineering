/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireCollisionData.ConnectionSegments;
import blusunrize.immersiveengineering.mixin.accessors.client.CompiledChunkAccess;
import blusunrize.immersiveengineering.mixin.accessors.client.RenderChunkAccess;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/*
TODO:
 - Sync wires early in case a "middle" chunk is synced first
 - Special treatment vertical wires
 - Write a comment for Forge about threading, maybe
 */
public class ConnectionRenderer implements ResourceManagerReloadListener
{
	private static final LoadingCache<SegmentKey, RenderedSegment> SEGMENT_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(120, TimeUnit.SECONDS)
			.build(CacheLoader.from(ConnectionRenderer::renderSegment));
	private static final ResettableLazy<TextureAtlasSprite> WIRE_TEXTURE = new ResettableLazy<>(
			() -> Minecraft.getInstance().getModelManager()
					.getAtlas(InventoryMenu.BLOCK_ATLAS)
					.getSprite(ImmersiveEngineering.rl("block/wire"))
	);

	@Override
	public void onResourceManagerReload(@Nonnull ResourceManager pResourceManager)
	{
		WIRE_TEXTURE.reset();
		resetCache();
	}

	public static void resetCache()
	{
		SEGMENT_CACHE.invalidateAll();
	}

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
		List<ConnectionSegments> connectionParts = globalNet.getCollisionData().getWiresIn(section);
		if(connectionParts==null||connectionParts.isEmpty())
			return;
		RenderType renderType = RenderType.solid();
		BufferBuilder builder = buffers.builder(renderType);
		CompiledChunkAccess compiledAccess = (CompiledChunkAccess)compiled;
		if(compiledAccess.getHasLayer().add(renderType))
			((RenderChunkAccess)renderChunk).invokeBeginLayer(builder);
		for(ConnectionSegments connection : connectionParts)
		{
			ConnectionPoint connectionOrigin = connection.connection().getEndA();
			renderSegments(
					builder, connection,
					connectionOrigin.getX()-chunkOrigin.getX(),
					connectionOrigin.getY()-chunkOrigin.getY(),
					connectionOrigin.getZ()-chunkOrigin.getZ(),
					region
			);
		}
		compiledAccess.setIsCompletelyEmpty(false);
		compiledAccess.getHasBlocks().add(renderType);
	}

	public static void renderSegments(
			VertexConsumer out, ConnectionSegments toRender, int offX, int offY, int offZ, BlockAndTintGetter level
	)
	{
		Connection connection = toRender.connection();
		int color = connection.type.getColour(connection);
		double radius = connection.type.getRenderDiameter()/2;
		int lastLight = 0;
		for(int startPoint = toRender.firstPointToRender(); startPoint < toRender.lastPointToRender(); ++startPoint)
		{
			RenderedSegment renderedSegment = SEGMENT_CACHE.getUnchecked(
					new SegmentKey(radius, color, connection.getCatenaryData(), startPoint)
			);
			if(startPoint==toRender.firstPointToRender())
				lastLight = getLight(connection, renderedSegment.offsetStart, level);
			int nextLight = getLight(connection, renderedSegment.offsetEnd, level);
			renderedSegment.render(lastLight, nextLight, OverlayTexture.NO_OVERLAY, offX, offY, offZ, out);
			lastLight = nextLight;
		}
	}

	public static void renderConnection(
			VertexConsumer out,
			CatenaryData catenaryData, double radius, int color,
			int light, int overlay
	)
	{
		for(int i = 0; i < Connection.RENDER_POINTS_PER_WIRE; i++)
			SEGMENT_CACHE.getUnchecked(new SegmentKey(radius, color, catenaryData, i))
					.render(light, light, overlay, 0, 0, 0, out);
	}

	private static RenderedSegment renderSegment(SegmentKey key)
	{
		CatenaryData catenaryData = key.catenaryShape();
		List<Vertex> vertices = new ArrayList<>(4*4);
		Vec3 start = key.catenaryShape().getRenderPoint(key.startIndex());
		Vec3 end = key.catenaryShape().getRenderPoint(key.startIndex()+1);
		Vec3 horNormal = new Vec3(-catenaryData.delta().z, 0, catenaryData.delta().x).normalize();
		Vec3 horRadius = horNormal.scale(key.radius());
		Vec3 verticalNormal = start.subtract(end).cross(horNormal).normalize();
		Vec3 verticalRadius = verticalNormal.scale(-key.radius());

		renderBidirectionalQuad(vertices, start, end, horRadius, key.color(), verticalNormal);
		renderBidirectionalQuad(vertices, start, end, verticalRadius, key.color(), horNormal);
		return new RenderedSegment(vertices, new Vec3i(start.x, start.y, start.z), new Vec3i(end.x, end.y, end.z));
	}

	private static int getLight(Connection connection, Vec3i point, BlockAndTintGetter level)
	{
		return LevelRenderer.getLightColor(level, connection.getEndA().getPosition().offset(point));
	}

	//TODO move somewhere else
	private static int getByte(int value, int lowestBit)
	{
		return (value >> lowestBit)&255;
	}

	private static void renderBidirectionalQuad(
			List<Vertex> out, Vec3 start, Vec3 end, Vec3 radius, int color, Vec3 positiveNormal
	)
	{
		TextureAtlasSprite texture = WIRE_TEXTURE.get();
		UVCoords[] uvs = {
				new UVCoords(texture.getU0(), texture.getV0()),
				new UVCoords(texture.getU1(), texture.getV0()),
				new UVCoords(texture.getU1(), texture.getV1()),
				new UVCoords(texture.getU0(), texture.getV1()),
		};
		Vec3[] vertices = {start.add(radius), end.add(radius), end.subtract(radius), start.subtract(radius),};
		for(int i = 0; i < vertices.length; i++)
			out.add(vertex(vertices[i], uvs[i], color, positiveNormal, i==0||i==3));
		for(int i = vertices.length-1; i >= 0; i--)
			out.add(vertex(vertices[i], uvs[i], color, positiveNormal.scale(-1), i==0||i==3));
	}

	private static Vertex vertex(Vec3 point, UVCoords uv, int color, Vec3 normal, boolean lightForStart)
	{
		return new Vertex(
				(float)point.x, (float)point.y, (float)point.z,
				(float)uv.u(), (float)uv.v(),
				getByte(color, 0)/255f, getByte(color, 8)/255f, getByte(color, 16)/255f,
				(float)normal.x, (float)normal.y, (float)normal.y,
				lightForStart
		);
	}

	private record SegmentKey(double radius, int color, CatenaryData catenaryShape, int startIndex)
	{
	}

	private record Vertex(
			float posX, float posY, float posZ,
			float texU, float texV,
			float red, float green, float blue,
			float normalX, float normalY, float normalZ,
			boolean lightForStart
	)
	{
	}

	private record RenderedSegment(List<Vertex> vertices, Vec3i offsetStart, Vec3i offsetEnd)
	{
		public void render(int lightStart, int lightEnd, int overlay, int offX, int offY, int offZ, VertexConsumer out)
		{
			for(Vertex v : vertices)
				out.vertex(
						offX+v.posX, offY+v.posY, offZ+v.posZ,
						v.red, v.green, v.blue, 1,
						v.texU, v.texV,
						overlay, v.lightForStart?lightStart: lightEnd,
						v.normalX, v.normalY, v.normalZ
				);
		}
	}
}
