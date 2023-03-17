/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import com.google.common.collect.HashMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;

public class BlueprintRenderer
{
	public static final RenderType RENDER_TYPE = IERenderTypes.POSITION_COLOR_LIGHTMAP;
	private static final HashMap<BlueprintCraftingRecipe, BlueprintLines> BLUEPRINT_CACHE = new HashMap<>();

	public static BlueprintLines getBlueprintDrawable(BlueprintCraftingRecipe recipe, Level world)
	{
		if(recipe==null)
			return null;
		BlueprintLines blueprint = BLUEPRINT_CACHE.get(recipe);
		if(blueprint==null)
		{
			blueprint = getBlueprintDrawable(recipe.output.get(), world);
			BLUEPRINT_CACHE.put(recipe, blueprint);
		}
		return blueprint;
	}

	public static BlueprintLines getBlueprintDrawable(ItemStack stack, Level world)
	{
		if(stack.isEmpty())
			return null;
		Player player = ClientUtils.mc().player;
		List<TextureAtlasSprite> images = new ArrayList<>();
		try
		{
			BakedModel ibakedmodel = ClientUtils.mc().getItemRenderer().getModel(stack, world, player, 0);
			Set<ResourceLocation> textures = new HashSet<>();
			Collection<BakedQuad> quads = ibakedmodel.getQuads(null, null, world.random, ModelData.EMPTY, null);
			final Function<ResourceLocation, TextureAtlasSprite> blockAtlas = ClientUtils.mc().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS);
			for(BakedQuad quad : quads)
			{
				final ResourceLocation texture = quad.getSprite().contents().name();
				if(textures.add(texture))
					images.add(blockAtlas.apply(texture));
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		if(images.isEmpty())
			return null;
		List<Pair<TexturePoint, TexturePoint>> lines = new ArrayList<>();
		Set<TexturePoint> testSet = new HashSet<>();
		HashMultimap<Integer, TexturePoint> area = HashMultimap.create();
		int wMax = 0;
		for(TextureAtlasSprite bufferedImage : images)
		{
			Set<Pair<TexturePoint, TexturePoint>> tempLines = new HashSet<>();

			int w = bufferedImage.contents().width();
			int h = bufferedImage.contents().height();

			if(h > w)
				h = w;
			if(w > wMax)
				wMax = w;
			for(int hh = 0; hh < h; hh++)
				for(int ww = 0; ww < w; ww++)
				{
					int argb = bufferedImage.getPixelRGBA(0, ww, hh);
					float r = (argb>>16&255)/255f;
					float g = (argb>>8&255)/255f;
					float b = (argb&255)/255f;
					float intesity = (r+b+g)/3f;
					int alpha = (argb>>24)&255;
					if(alpha > 0)
					{
						boolean added = false;
						//Check colour sets for similar colour to shade it later
						TexturePoint tp = new TexturePoint(ww, hh, w);
						if(!testSet.contains(tp))
						{
							for(Integer key : area.keySet())
							{
								for(TexturePoint p : area.get(key))
								{
									float mod = w/(float)p.scale;
									int pColour = bufferedImage.getPixelRGBA(0, (int)(p.x*mod), (int)(p.y*mod));
									float dR = (r-(pColour>>16&255)/255f);
									float dG = (g-(pColour>>8&255)/255f);
									float dB = (b-(pColour&255)/255f);
									double delta = Math.sqrt(dR*dR+dG*dG+dB*dB);
									if(delta < .25)
									{
										area.put(key, tp);
										added = true;
										break;
									}
								}
								if(added)
									break;
							}
							if(!added)
								area.put(argb, tp);
							testSet.add(tp);
						}
						//Compare to direct neighbour
						for(int i = 0; i < 4; i++)
						{
							int xx = (i==0?-1: i==1?1: 0);
							int yy = (i==2?-1: i==3?1: 0);
							int u = ww+xx;
							int v = hh+yy;

							int neighbour = 0;
							float delta = 1;
							boolean notTransparent = false;
							if(u >= 0&&u < w&&v >= 0&&v < h)
							{
								neighbour = bufferedImage.getPixelRGBA(0, u, v);
								notTransparent = ((neighbour>>24)&255) > 0;
								if(notTransparent)
								{
									float neighbourIntesity = ((neighbour>>16&255)+(neighbour>>8&255)+(neighbour&255))/765f;
									float intesityDelta = Math.max(0, Math.min(1, Math.abs(intesity-neighbourIntesity)));
									float rDelta = Math.max(0, Math.min(1, Math.abs(r-(neighbour>>16&255)/255f)));
									float gDelta = Math.max(0, Math.min(1, Math.abs(g-(neighbour>>8&255)/255f)));
									float bDelta = Math.max(0, Math.min(1, Math.abs(b-(neighbour&255)/255f)));
									delta = Math.max(intesityDelta, Math.max(rDelta, Math.max(gDelta, bDelta)));
									delta = delta < .25?0: delta > .4?1: delta;
								}
							}
							if(delta > 0)
							{
								Pair<TexturePoint, TexturePoint> l = Pair.of(new TexturePoint(ww+(i==0?0: i==1?1: 0), hh+(i==2?0: i==3?1: 0), w), new TexturePoint(ww+(i==0?0: i==1?1: 1), hh+(i==2?0: i==3?1: 1), w));
								tempLines.add(l);
							}
						}
					}
				}
			lines.addAll(tempLines);
		}

		List<Integer> lumiSort = new ArrayList<>(area.keySet());
		lumiSort.sort(Comparator.comparingDouble(BlueprintRenderer::getLuminance));
		HashMultimap<ShadeStyle, Point> complete_areaMap = HashMultimap.create();
		int lineNumber = 2;
		StripeDirection lineStyle = StripeDirection.VERTICAL;
		for(Integer i : lumiSort)
		{
			Set<Point> styleSlot = complete_areaMap.get(new ShadeStyle(lineNumber, lineStyle));
			for(TexturePoint point : area.get(i))
				styleSlot.add(new Point(point.x(), point.y()));
			lineStyle = lineStyle.next();
			if(lineStyle==StripeDirection.VERTICAL)
				lineNumber += 1;
		}

		Set<Pair<Point, Point>> complete_lines = new HashSet<>();
		for(Pair<TexturePoint, TexturePoint> line : lines)
		{
			TexturePoint p1 = line.getFirst();
			TexturePoint p2 = line.getSecond();
			complete_lines.add(Pair.of(new Point((int)(p1.x/(float)p1.scale*wMax), (int)(p1.y/(float)p1.scale*wMax)), new Point((int)(p2.x/(float)p2.scale*wMax), (int)(p2.y/(float)p2.scale*wMax))));
		}
		return new BlueprintLines(wMax, complete_lines, complete_areaMap);
	}

	private static void putLineVertex(
			PoseStack.Pose transform, VertexConsumer out, float x, float z, Vector3f normalUp, int light
	)
	{
		Vector4f position = new Vector4f(x, z, 0, 1);
		position.mul(transform.pose()).div(position.w);
		out.vertex(
				position.x(), position.y(), position.z(),
				1, 1, 1, 1,
				0.5f, 0.5f,
				OverlayTexture.NO_OVERLAY,
				light,
				normalUp.x(), normalUp.y(), normalUp.z()
		);
	}

	private static LinePainter makeQuadLinePainter(PoseStack.Pose transform, VertexConsumer out, int light)
	{
		Vector3f up = new Vector3f(0, 1, 0);
		up.mul(transform.normal());
		return (x0, y0, x1, y1, width) -> {
			float deltaX = x1-x0;
			float deltaY = y1-y0;
			// Normalize
			final double distance = Mth.invSqrt(deltaY*deltaY+deltaX*deltaX);
			deltaX /= distance;
			deltaY /= distance;
			// Draw quad
			final float offsetX = -deltaY*width;
			final float offsetY = deltaX*width;
			putLineVertex(transform, out, x0+offsetX, y0+offsetY, up, light);
			putLineVertex(transform, out, x1+offsetX, y1+offsetY, up, light);
			putLineVertex(transform, out, x1-offsetX, y1-offsetY, up, light);
			putLineVertex(transform, out, x0-offsetX, y0-offsetY, up, light);
		};
	}

	private static double getLuminance(int rgb)
	{
		return Math.sqrt(.241*(rgb>>16&255)+.691*(rgb>>8&255)+.068*(rgb&255));
	}

	public static class BlueprintLines
	{
		final int textureScale;
		final Set<Pair<Point, Point>> lines;
		final HashMultimap<ShadeStyle, Point> areas;

		BlueprintLines(int textureScale, Set<Pair<Point, Point>> lines, HashMultimap<ShadeStyle, Point> areas)
		{
			this.textureScale = textureScale;
			this.lines = lines;
			this.areas = areas;
		}

		public int getTextureScale()
		{
			return textureScale;
		}

		public void draw(PoseStack matrixStack, MultiBufferSource buffer, int packedLight)
		{
			draw(matrixStack, buffer.getBuffer(RENDER_TYPE), packedLight);
		}

		public void draw(PoseStack matrixStack, VertexConsumer baseBuilder, int packedLight)
		{
			LinePainter painter = makeQuadLinePainter(matrixStack.last(), baseBuilder, packedLight);
			for(Pair<Point, Point> line : lines)
				painter.drawLine(line.getFirst().x, line.getFirst().y, line.getSecond().x, line.getSecond().y, 0.2f);

			for(ShadeStyle style : areas.keySet())
				for(Point pixel : areas.get(style))
					style.drawShading(pixel, painter);
		}
	}

	private record ShadeStyle(int stripeAmount, StripeDirection stripeDirection)
	{
		void drawShading(Point pixel, LinePainter painter)
		{
			float step = 1/(float)stripeAmount;
			float offset = step/2;
			if(stripeDirection==StripeDirection.DIAGONAL)
			{
				int perSide = stripeAmount/2+stripeAmount%2;
				step = 1/(float)perSide;
				offset = stripeAmount%2==0?step/2: step;
			}
			float width = 0.1f;
			for(int i = 0; i < stripeAmount; i++)
				switch(stripeDirection)
				{
					case VERTICAL ->
							painter.drawLine(pixel.x+offset+step*i, pixel.y, pixel.x+offset+step*i, pixel.y+1, width);
					case HORIZONTAL ->
							painter.drawLine(pixel.x, pixel.y+offset+step*i, pixel.x+1, pixel.y+offset+step*i, width);
					case DIAGONAL ->
					{
						if(i==stripeAmount-1&&stripeAmount%2==1)
							painter.drawLine(pixel.x, pixel.y+1, pixel.x+1, pixel.y, width);
						else if(i%2==0)
							painter.drawLine(pixel.x, pixel.y+offset+step*(i/2), pixel.x+offset+step*(i/2), pixel.y, width);
						else
							painter.drawLine(pixel.x+1-offset-step*(i/2), pixel.y+1, pixel.x+1, pixel.y+1-offset-step*(i/2), width);
					}

				}

		}
	}

	private enum StripeDirection
	{
		VERTICAL, HORIZONTAL, DIAGONAL;

		public StripeDirection next()
		{
			return switch(this)
					{
						case VERTICAL -> HORIZONTAL;
						case HORIZONTAL -> DIAGONAL;
						case DIAGONAL -> VERTICAL;
					};
		}
	}

	private record TexturePoint(int x, int y, int scale)
	{
	}

	private interface LinePainter
	{
		void drawLine(float x0, float y0, float x1, float y1, float width);
	}
}
