/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.excavator.ExcavatorHandler;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.api.excavator.MineralVein;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent.Stage;
import org.apache.commons.lang3.mutable.MutableInt;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID, bus = Bus.GAME)
public class LevelStageRenders
{
	public static final Map<Connection, Pair<Collection<BlockPos>, MutableInt>> FAILED_CONNECTIONS = new HashMap<>();
	private static final boolean ENABLE_VEIN_DEBUG = false;

	@SubscribeEvent
	public static void onRenderLevelStage(RenderLevelStageEvent event)
	{
		if(event.getStage()==Stage.AFTER_PARTICLES&&!FractalParticle.PARTICLE_FRACTAL_DEQUE.isEmpty())
			renderFractalParticles(event);
		if(event.getStage()==Stage.AFTER_CUTOUT_BLOCKS)
			renderMineralVeinDebug(event);
		if(event.getStage()==Stage.AFTER_TRANSLUCENT_BLOCKS)
			renderFailedConnections(event);
	}

	private static void renderFractalParticles(RenderLevelStageEvent event)
	{
		float partial = event.getPartialTick().getGameTimeDeltaTicks();
		final Pair<PoseStack, BufferSource> context = prepare(event);
		List<Pair<RenderType, List<Consumer<VertexConsumer>>>> renders = new ArrayList<>();
		for(FractalParticle p : FractalParticle.PARTICLE_FRACTAL_DEQUE)
			for(Pair<RenderType, Consumer<VertexConsumer>> r : p.render(partial, context.getFirst()))
			{
				boolean added = false;
				for(Pair<RenderType, List<Consumer<VertexConsumer>>> e : renders)
					if(e.getFirst().equals(r.getFirst()))
					{
						e.getSecond().add(r.getSecond());
						added = true;
						break;
					}
				if(!added)
					renders.add(Pair.of(r.getFirst(), new ArrayList<>(List.of(r.getSecond()))));
			}
		for(Pair<RenderType, List<Consumer<VertexConsumer>>> entry : renders)
		{
			VertexConsumer bb = context.getSecond().getBuffer(entry.getFirst());
			for(Consumer<VertexConsumer> render : entry.getSecond())
				render.accept(bb);
		}
		finish(context);
		FractalParticle.PARTICLE_FRACTAL_DEQUE.clear();
	}

	private static void renderMineralVeinDebug(RenderLevelStageEvent event)
	{
		// !isProduction: Safety feature to make sure this doesn't run even if the enable flag is left on by accident
		boolean show = ENABLE_VEIN_DEBUG&&!FMLLoader.isProduction();
		if(!show)
			return;
		// Default <=> shift is sneak, use ctrl instead
		if(Minecraft.getInstance().options.keyShift.isDefault())
			show = Screen.hasControlDown();
		else
			show = Screen.hasShiftDown();
		if(!show)
			return;
		final Pair<PoseStack, BufferSource> context = prepare(event);
		final PoseStack transform = context.getFirst();
		ResourceKey<Level> dimension = mc().player.getCommandSenderWorld().dimension();
		List<ResourceLocation> keyList = new ArrayList<>(MineralMix.RECIPES.getRecipeNames(mc().level));
		keyList.sort(Comparator.comparing(ResourceLocation::toString));
		BlockPos feetPos = mc().player.blockPosition();
		final ColumnPos playerCol = new ColumnPos(feetPos.getX(), feetPos.getZ());
		// 24: very roughly 16 * sqrt(2)
		final long maxDistance = mc().options.renderDistance().get()*24L;
		final long maxDistanceSq = maxDistance*maxDistance;
		Multimap<ResourceKey<Level>, MineralVein> minerals;
		final var minHeight = mc().level.getMinBuildHeight();
		final var maxHeight = mc().level.getMaxBuildHeight();
		synchronized(minerals = ExcavatorHandler.getMineralVeinList())
		{
			for(MineralVein vein : minerals.get(dimension))
			{
				MineralMix mineral = vein.getMineral(mc().level);
				if(mineral==null)
					continue;
				ColumnPos pos = vein.getPos();
				final long xDiff = pos.x()-playerCol.x();
				final long zDiff = pos.z()-playerCol.z();
				long distToPlayerSq = xDiff*xDiff+zDiff*zDiff;
				if(distToPlayerSq > maxDistanceSq)
					continue;
				int iC = keyList.indexOf(vein.getMineralName());
				DyeColor color = DyeColor.values()[iC%16];
				var rgb = Utils.vec4fFromDye(color);
				transform.pushPose();
				transform.translate(pos.x(), 0, pos.z());
				VertexConsumer bufferBuilder = context.getSecond().getBuffer(IERenderTypes.CHUNK_MARKER);
				Matrix4f mat = transform.last().pose();
				bufferBuilder.addVertex(mat, 0, minHeight, 0).setColor(rgb.x, rgb.y, rgb.z, .75f).setNormal(transform.last(), 0, 1, 0);
				bufferBuilder.addVertex(mat, 0, maxHeight, 0).setColor(rgb.x, rgb.y, rgb.z, .75f).setNormal(transform.last(), 0, 1, 0);
				int radius = vein.getRadius();
				List<Vector3f> positions = new ArrayList<>();
				for(int p = 0; p < 12; p++)
				{
					final float angle = 360.0f/12*p;
					final double x1 = radius*Math.cos(angle*Math.PI/180);
					final double z1 = radius*Math.sin(angle*Math.PI/180);
					positions.add(new Vector3f((float)x1, (float)(Minecraft.getInstance().player.position().y+10), (float)z1));
				}
				for(int p = 0; p < 12; p++)
				{
					Vector3f pointA = positions.get(p);
					Vector3f pointB = positions.get((p+1)%positions.size());
					Vector3f diff = new Vector3f(pointB);
					diff.sub(pointA);
					diff.normalize();
					for(Vector3f point : List.of(pointA, pointB))
						bufferBuilder.addVertex(mat, point.x(), point.y(), point.z())
								.setColor(rgb.x, rgb.y, rgb.z, .75f)
								//Not actually a normal, just the direction of the line
								.setNormal(transform.last(), diff.x(), diff.y(), diff.z());
				}
				transform.popPose();
			}
		}
		finish(context);
	}

	private static void renderFailedConnections(RenderLevelStageEvent event)
	{
		final Pair<PoseStack, BufferSource> context = prepare(event);
		VertexConsumer builder = context.getSecond().getBuffer(IERenderTypes.CHUNK_MARKER);
		final PoseStack transform = context.getFirst();
		for(Entry<Connection, Pair<Collection<BlockPos>, MutableInt>> entry : FAILED_CONNECTIONS.entrySet())
		{
			Connection conn = entry.getKey();
			transform.pushPose();
			transform.translate(conn.getEndA().getX(), conn.getEndA().getY(), conn.getEndA().getZ());
			Matrix4f mat = transform.last().pose();
			int time = entry.getValue().getSecond().intValue();
			float alpha = (float)Math.min((2+Math.sin(time*Math.PI/40))/3, time/20F);
			Vec3 prev = conn.getPoint(0, conn.getEndA());
			for(int i = 0; i < Connection.RENDER_POINTS_PER_WIRE; i++)
			{
				Vec3 next = conn.getCatenaryData().getRenderPoint(i+1);
				Vec3 diff = next.subtract(prev).normalize();
				builder.addVertex(mat, (float)prev.x, (float)prev.y, (float)prev.z)
						.setColor(1, 0, 0, alpha)
						.setNormal(transform.last(), (float)diff.x, (float)diff.y, (float)diff.z);
				alpha = (float)Math.min((2+Math.sin((time+(i+1)*8)*Math.PI/40))/3, time/20F);
				builder.addVertex(mat, (float)next.x, (float)next.y, (float)next.z)
						.setColor(1, 0, 0, alpha)
						.setNormal(transform.last(), (float)diff.x, (float)diff.y, (float)diff.z);
				prev = next;
			}
			transform.popPose();
		}
		renderObstructingBlocks(context);
		finish(context);
	}

	private static void renderObstructingBlocks(Pair<PoseStack, BufferSource> context)
	{
		final PoseStack transform = context.getFirst();
		TransformingVertexBuilder builder = new TransformingVertexBuilder(
				context.getSecond(), IERenderTypes.TRANSLUCENT_POSITION_COLOR
		);
		builder.defaultColor(255, 0, 0, 128);
		for(Entry<Connection, Pair<Collection<BlockPos>, MutableInt>> entry : FAILED_CONNECTIONS.entrySet())
		{
			for(BlockPos obstruction : entry.getValue().getFirst())
			{
				transform.pushPose();
				transform.translate(obstruction.getX(), obstruction.getY(), obstruction.getZ());
				final float eps = 1e-3f;
				RenderUtils.renderBox(builder, transform, -eps, -eps, -eps, 1+eps, 1+eps, 1+eps);
				transform.popPose();
			}
		}
		builder.unsetDefaultColor();
	}

	private static Pair<PoseStack, MultiBufferSource.BufferSource> prepare(RenderLevelStageEvent event)
	{
		PoseStack transform = event.getPoseStack();
		transform.pushPose();
		final Vec3 cameraPos = event.getCamera().getPosition();
		transform.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
		MultiBufferSource.BufferSource buffers = mc().renderBuffers().bufferSource();
		return Pair.of(transform, buffers);
	}

	private static void finish(Pair<PoseStack, MultiBufferSource.BufferSource> context)
	{
		context.getSecond().endBatch();
		context.getFirst().popPose();
	}
}
