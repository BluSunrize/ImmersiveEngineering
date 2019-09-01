/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAutoWorkbench;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.HashMultimap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

public class TileRenderAutoWorkbench extends TileEntitySpecialRenderer<TileEntityAutoWorkbench>
{
	@Override
	public void render(TileEntityAutoWorkbench te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(!te.formed||te.isDummy()||!te.getWorld().isBlockLoaded(te.getPos(), false))
			return;

		//Grab model + correct eextended state
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=IEContent.blockMetalMultiblock)
			return;
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);

		//Initialize Tesselator and BufferBuilder
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		//Outer GL Wrapping, initial translation
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);
		if(te.mirrored)
			GlStateManager.scale(te.facing.getXOffset()==0?-1: 1, 1, te.facing.getZOffset()==0?-1: 1);

		//Item Displacement
		float[][] itemDisplays = new float[te.processQueue.size()][];
		//Animations
		float drill = 0;
		float lift = 0;
		float press = 0;
		float liftPress = 0;

		for(int i = 0; i < itemDisplays.length; i++)
		{
			MultiblockProcess<IMultiblockRecipe> process = te.processQueue.get(i);
			if(process==null||process.processTick <= 0||process.processTick==process.maxTicks)
				continue;
			//+partialTicks
			float processTimer = ((float)process.processTick)/process.maxTicks*180;
			if(processTimer <= 9)
				continue;

			float itemX = -1;
			float itemY = -.34375f;
			float itemZ = -.9375f;
			float itemAngle = 90f;

			if(processTimer <= 24)//slide
			{
				itemAngle = 67.5f;
				if(processTimer <= 19)
				{
					itemZ += .25+(19-processTimer)/10f*.5f;
					itemY += .25+(19-processTimer)/10f*.21875f;
				}
				else
				{
					itemZ += (24-processTimer)/5f*.25f;
					itemY += (24-processTimer)/5f*.25f;
				}
			}
			else if(processTimer <= 40)
			{
				itemX += (processTimer-24)/16f;
			}
			else if(processTimer <= 100)
			{
				itemX += 1;
				float drillStep = 0;
				if(processTimer <= 60)
				{
					lift = (processTimer-40)/20f*.3125f;
					drillStep = 4+(60-processTimer)*4;
				}
				else if(processTimer <= 80)
				{
					lift = .3125f;
					drillStep = 4;
				}
				else
				{
					lift = (100-processTimer)/20f*.3125f;
					drillStep = 4+(processTimer-80)*4;
				}
				if(drillStep > 0)
					drill = processTimer%drillStep/drillStep*360;
				itemY += Math.max(0, lift-.0625);
			}
			else if(processTimer <= 116)
			{
				itemX += 1;
				itemZ += (processTimer-100)/16f;
			}
			else if(processTimer <= 132)
			{
				itemX += 1+(processTimer-116)/16f;
				itemZ += 1;
			}
			else if(processTimer <= 172)
			{
				itemX += 2;
				itemZ += 1;
				if(processTimer <= 142)
					press = (processTimer-132)/10f;
				else if(processTimer <= 162)
					press = 1;
				else
					press = (172-processTimer)/10f;
				liftPress = press*.0625f;
				itemY += liftPress;
			}
			else if(processTimer <= 180)
			{
				itemX += 2+(processTimer-172)/16f;
				itemZ += 1;
			}
			itemDisplays[i] = new float[]{processTimer, itemX, itemY, itemZ, itemAngle};

		}

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		ItemStack blueprintStack = te.inventory.get(0);
		if(!blueprintStack.isEmpty())
			renderModelPart(blockRenderer, tessellator, worldRenderer, te.getWorld(), state, model, blockPos, "blueprint");


		GlStateManager.translate(0, lift, 0);
		renderModelPart(blockRenderer, tessellator, worldRenderer, te.getWorld(), state, model, blockPos, "lift");
		GlStateManager.translate(0, -lift, 0);

		EnumFacing f = te.getFacing();
		float tx = f==EnumFacing.WEST?-.9375f: f==EnumFacing.EAST?.9375f: 0;
		float tz = f==EnumFacing.NORTH?-.9375f: f==EnumFacing.SOUTH?.9375f: 0;
		GlStateManager.translate(tx, 0, tz);
		GlStateManager.rotate(drill, 0, 1, 0);
		renderModelPart(blockRenderer, tessellator, worldRenderer, te.getWorld(), state, model, blockPos, "drill");
		GlStateManager.rotate(-drill, 0, 1, 0);
		GlStateManager.translate(-tx, 0, -tz);

		tx = f==EnumFacing.WEST?-.59375f: f==EnumFacing.EAST?.59375f: 0;
		tz = f==EnumFacing.NORTH?-.59375f: f==EnumFacing.SOUTH?.59375f: 0;
		GlStateManager.translate(tx, -.21875, tz);
		GlStateManager.rotate(press*90, -f.getZOffset(), 0, f.getXOffset());
		renderModelPart(blockRenderer, tessellator, worldRenderer, te.getWorld(), state, model, blockPos, "press");
		GlStateManager.rotate(-press*90, -f.getZOffset(), 0, f.getXOffset());
		GlStateManager.translate(-tx, .21875, -tz);

		GlStateManager.translate(0, liftPress, 0);
		renderModelPart(blockRenderer, tessellator, worldRenderer, te.getWorld(), state, model, blockPos, "pressLift");
		GlStateManager.translate(0, -liftPress, 0);

		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();

		switch(f)
		{
			case NORTH:
				break;
			case SOUTH:
				GlStateManager.rotate(180, 0, 1, 0);
				break;
			case WEST:
				GlStateManager.rotate(90, 0, 1, 0);
				break;
			case EAST:
				GlStateManager.rotate(-90, 0, 1, 0);
				break;
		}

		//DRAW ITEMS HERE
		for(int i = 0; i < itemDisplays.length; i++)
			if(itemDisplays[i]!=null)
			{
				MultiblockProcess<IMultiblockRecipe> process = te.processQueue.get(i);
				if(process==null||!(process instanceof MultiblockProcessInWorld))
					continue;

				float scale = .3125f;
				List<ItemStack> dList = ((MultiblockProcessInWorld)process).getDisplayItem();
				if(!dList.isEmpty())
					if(dList.size() < 2)
					{
						GlStateManager.translate(itemDisplays[i][1], itemDisplays[i][2], itemDisplays[i][3]);
						GlStateManager.rotate(itemDisplays[i][4], 1, 0, 0);
						GlStateManager.scale(scale, scale, .5f);
						ClientUtils.mc().getRenderItem().renderItem(dList.get(0), ItemCameraTransforms.TransformType.FIXED);
						GlStateManager.scale(1/scale, 1/scale, 2);
						GlStateManager.rotate(-itemDisplays[i][4], 1, 0, 0);
						GlStateManager.translate(-itemDisplays[i][1], -itemDisplays[i][2], -itemDisplays[i][3]);
					}
					else
					{
						int size = dList.size();
						int lines = (int)Math.ceil(size/2f);
						float spacer = (lines-1)*.234375f;
						for(int d = 0; d < size; d++)
						{
							float oX = (size > 2?-.3125f: 0)+(lines-d/2)*.0625f+d%2*.3125f;
							float oZ = -spacer/2f+d/2*.234375f;
							float oY = 0;

							float localItemX = itemDisplays[i][1]+oX;
							float localItemY = itemDisplays[i][2]+oY;
							float localItemZ = itemDisplays[i][3]+oZ;
							float subProcess = itemDisplays[i][0]-d/2*4;
							float localAngle = itemDisplays[i][4];
							if(subProcess <= 24)//slide
							{
								localAngle = 67.5f;
								if(subProcess <= 19)
								{
									localItemZ = -1+.25f+(19-subProcess)/10f*.5f;
									localItemY = -.34375f+.25f+(19-subProcess)/10f*.21875f;
								}
								else
								{
									localItemZ = -1+(oZ-(24-subProcess)/5f*oZ);
									localItemY = -.34375f+(24-subProcess)/5f*.25f;
								}
							}
							GlStateManager.translate(localItemX, localItemY, localItemZ);
							GlStateManager.rotate(localAngle, 1, 0, 0);
							GlStateManager.scale(scale, scale, .5f);
							ClientUtils.mc().getRenderItem().renderItem(dList.get(d), ItemCameraTransforms.TransformType.FIXED);
							GlStateManager.scale(1/scale, 1/scale, 2);
							GlStateManager.rotate(-localAngle, 1, 0, 0);
							GlStateManager.translate(-localItemX, -localItemY, -localItemZ);
						}
					}
			}

		//Blueprint
		double playerDistanceSq = ClientUtils.mc().player.getDistanceSq(blockPos);

		if(!Config.IEConfig.disableFancyBlueprints&&!blueprintStack.isEmpty()&&playerDistanceSq < 1000)
		{
			BlueprintCraftingRecipe[] recipes = BlueprintCraftingRecipe.findRecipes(ItemNBTHelper.getString(blueprintStack, "blueprint"));
			BlueprintCraftingRecipe recipe = (te.selectedRecipe < 0||te.selectedRecipe >= recipes.length)?null: recipes[te.selectedRecipe];
			BlueprintLines blueprint = recipe==null?null: getBlueprintDrawable(recipe, te.getWorld());
			if(blueprint!=null)
			{
				//Width depends on distance
				float lineWidth = playerDistanceSq < 6?3: playerDistanceSq < 25?2: playerDistanceSq < 40?1: .5f;
				GlStateManager.translate(-.195, .125, .97);
				GlStateManager.rotate(-45, 1, 0, 0);
				GlStateManager.disableCull();
				GlStateManager.disableTexture2D();
				GlStateManager.enableBlend();
				float scale = .0375f/(blueprint.textureScale/16f);
				GlStateManager.scale(scale, -scale, scale);
				GlStateManager.color(1, 1, 1, 1);
				blueprint.draw(lineWidth);
				GlStateManager.scale(1/scale, -1/scale, 1/scale);
				GlStateManager.enableAlpha();
				GlStateManager.enableTexture2D();
				GlStateManager.enableCull();
			}
		}
		GlStateManager.popMatrix();
	}

	public static void renderModelPart(final BlockRendererDispatcher blockRenderer, Tessellator tessellator, BufferBuilder worldRenderer, World world, IBlockState state, IBakedModel model, BlockPos pos, String... parts)
	{
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(Arrays.asList(parts), true));

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5-pos.getX(), -.5-pos.getY(), -.5-pos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(world, model, state, pos, worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
	}

	public static HashMap<BlueprintCraftingRecipe, BlueprintLines> blueprintCache = new HashMap<BlueprintCraftingRecipe, BlueprintLines>();

	public static BlueprintLines getBlueprintDrawable(BlueprintCraftingRecipe recipe, World world)
	{
		if(recipe==null)
			return null;
		BlueprintLines blueprint = blueprintCache.get(recipe);
		if(blueprint==null)
		{
			blueprint = getBlueprintDrawable(recipe.output, world);
			blueprintCache.put(recipe, blueprint);
		}
		return blueprint;
	}

	public static BlueprintLines getBlueprintDrawable(ItemStack stack, World world)
	{
		if(stack.isEmpty())
			return null;
		EntityPlayer player = ClientUtils.mc().player;
		ArrayList<BufferedImage> images = new ArrayList<>();
		try
		{
			IBakedModel ibakedmodel = ClientUtils.mc().getRenderItem().getItemModelWithOverrides(stack, world, player);
			if(ibakedmodel==null||ibakedmodel.isGui3d())
				return new BlueprintLinesEmpty();
			HashSet<String> textures = new HashSet();
			Collection<BakedQuad> quads = ibakedmodel.getQuads(null, null, 0);
			for(BakedQuad quad : quads)
				if(quad!=null&&quad.getSprite()!=null)
					textures.add(quad.getSprite().getIconName());
			for(String s : textures)
			{
				ResourceLocation rl = new ResourceLocation(s);
				rl = new ResourceLocation(rl.getNamespace(), String.format("%s/%s%s", "textures", rl.getPath(), ".png"));
				IResource resource = ClientUtils.mc().getResourceManager().getResource(rl);
				BufferedImage bufferedImage = TextureUtil.readBufferedImage(resource.getInputStream());
				if(bufferedImage!=null)
					images.add(bufferedImage);
			}
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		if(images.isEmpty())
			return null;
		ArrayList<Pair<TexturePoint, TexturePoint>> lines = new ArrayList();
		HashSet testSet = new HashSet();
		HashMultimap<Integer, TexturePoint> area = HashMultimap.create();
		int wMax = 0;
		for(BufferedImage bufferedImage : images)
		{
			Set<Pair<TexturePoint, TexturePoint>> temp_lines = new HashSet<>();

			int w = bufferedImage.getWidth();
			int h = bufferedImage.getHeight();

			if(h > w)
				h = w;
			if(w > wMax)
				wMax = w;
			for(int hh = 0; hh < h; hh++)
				for(int ww = 0; ww < w; ww++)
				{
					int argb = bufferedImage.getRGB(ww, hh);
					float r = (argb >> 16&255)/255f;
					float g = (argb >> 8&255)/255f;
					float b = (argb&255)/255f;
					float intesity = (r+b+g)/3f;
					int alpha = (argb >> 24)&255;
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
									int pColour = bufferedImage.getRGB((int)(p.x*mod), (int)(p.y*mod));
									float dR = (r-(pColour >> 16&255)/255f);
									float dG = (g-(pColour >> 8&255)/255f);
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
								neighbour = bufferedImage.getRGB(u, v);
								notTransparent = ((neighbour >> 24)&255) > 0;
								if(notTransparent)
								{
									float neighbourIntesity = ((neighbour >> 16&255)+(neighbour >> 8&255)+(neighbour&255))/765f;
									float intesityDelta = Math.max(0, Math.min(1, Math.abs(intesity-neighbourIntesity)));
									float rDelta = Math.max(0, Math.min(1, Math.abs(r-(neighbour >> 16&255)/255f)));
									float gDelta = Math.max(0, Math.min(1, Math.abs(g-(neighbour >> 8&255)/255f)));
									float bDelta = Math.max(0, Math.min(1, Math.abs(b-(neighbour&255)/255f)));
									delta = Math.max(intesityDelta, Math.max(rDelta, Math.max(gDelta, bDelta)));
									delta = delta < .25?0: delta > .4?1: delta;
								}
							}
							if(delta > 0)
							{
								Pair<TexturePoint, TexturePoint> l = Pair.of(new TexturePoint(ww+(i==0?0: i==1?1: 0), hh+(i==2?0: i==3?1: 0), w), new TexturePoint(ww+(i==0?0: i==1?1: 1), hh+(i==2?0: i==3?1: 1), w));
								temp_lines.add(l);
							}
						}
					}
				}
			lines.addAll(temp_lines);
		}

		ArrayList<Integer> lumiSort = new ArrayList<>(area.keySet());
		Collections.sort(lumiSort, (rgb1, rgb2) -> Double.compare(getLuminance(rgb1), getLuminance(rgb2)));
		HashMultimap<ShadeStyle, Point> complete_areaMap = HashMultimap.create();
		int lineNumber = 2;
		int lineStyle = 0;
		for(Integer i : lumiSort)
		{
			complete_areaMap.putAll(new ShadeStyle(lineNumber, lineStyle), area.get(i));
			++lineStyle;
			lineStyle %= 3;
			if(lineStyle==0)
				lineNumber += 1;
		}

		Set<Pair<Point, Point>> complete_lines = new HashSet<>();
		for(Pair<TexturePoint, TexturePoint> line : lines)
		{
			TexturePoint p1 = line.getKey();
			TexturePoint p2 = line.getValue();
			complete_lines.add(Pair.of(new Point((int)(p1.x/(float)p1.scale*wMax), (int)(p1.y/(float)p1.scale*wMax)), new Point((int)(p2.x/(float)p2.scale*wMax), (int)(p2.y/(float)p2.scale*wMax))));
		}
		return new BlueprintLines(wMax, complete_lines, complete_areaMap);
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

		public void draw(float lineWidth)
		{
			//Draw edges
			GlStateManager.glLineWidth(lineWidth);
			GlStateManager.glBegin(GL11.GL_LINES);
			for(Pair<Point, Point> line : lines)
			{
				GlStateManager.glVertex3f(line.getKey().x, line.getKey().y, 0);
				GlStateManager.glVertex3f(line.getValue().x, line.getValue().y, 0);
			}
			GlStateManager.glEnd();

			if(lineWidth >= 1)//Draw shading if player is close enough
			{
				GlStateManager.glLineWidth(lineWidth*.66f);
				GL11.glPointSize(4);
				GlStateManager.glBegin(GL11.GL_LINES);
				for(ShadeStyle style : areas.keySet())
					for(Point pixel : areas.get(style))
						style.drawShading(pixel);
				GlStateManager.glEnd();
			}
		}
	}

	public static class BlueprintLinesEmpty extends BlueprintLines
	{
		BlueprintLinesEmpty()
		{
			super(0, null, null);
		}

		@Override
		public void draw(float lineWidth)
		{
		}
	}

	private static class ShadeStyle
	{
		int stripeAmount = 1;
		int stripeDirection = 0;

		ShadeStyle(int stripeAmount, int stripeDirection)
		{
			this.stripeAmount = stripeAmount;
			this.stripeDirection = stripeDirection;
		}

		void drawShading(Point pixel)
		{
			float step = 1/(float)stripeAmount;
			float offset = step/2;
			if(stripeDirection > 1)
			{
				int perSide = stripeAmount/2+(stripeAmount%2==1?1: 0);
				step = 1/(float)(perSide);
				offset = stripeAmount%2==1?step: step/2;
			}
			for(int i = 0; i < stripeAmount; i++)
				if(stripeDirection==0)//vertical
				{
					GlStateManager.glVertex3f(pixel.x+offset+step*i, pixel.y, 0);
					GlStateManager.glVertex3f(pixel.x+offset+step*i, pixel.y+1, 0);
				}
				else if(stripeDirection==1)//horizontal
				{
					GlStateManager.glVertex3f(pixel.x, pixel.y+offset+step*i, 0);
					GlStateManager.glVertex3f(pixel.x+1, pixel.y+offset+step*i, 0);
				}
				else if(stripeDirection==2)//diagonal
				{
					if(i==stripeAmount-1&&stripeAmount%2==1)
					{
						GlStateManager.glVertex3f(pixel.x, pixel.y+1, 0);
						GlStateManager.glVertex3f(pixel.x+1, pixel.y, 0);
					}
					else if(i%2==0)
					{
						GlStateManager.glVertex3f(pixel.x, pixel.y+offset+step*(i/2), 0);
						GlStateManager.glVertex3f(pixel.x+offset+step*(i/2), pixel.y, 0);
					}
					else
					{
						GlStateManager.glVertex3f(pixel.x+1-offset-step*(i/2), pixel.y+1, 0);
						GlStateManager.glVertex3f(pixel.x+1, pixel.y+1-offset-step*(i/2), 0);
					}
				}
		}
	}

	private static class TexturePoint extends Point
	{
		final int scale;

		public TexturePoint(int x, int y, int scale)
		{
			super(x, y);
			this.scale = scale;
		}

		@Override
		public int hashCode()
		{
			return 31*(31*x+y)+scale;
		}
	}

	private static double getLuminance(int rgb)
	{
		return Math.sqrt(.241*(rgb >> 16&255)+.691*(rgb >> 8&255)+.068*(rgb&255));
	}
}
