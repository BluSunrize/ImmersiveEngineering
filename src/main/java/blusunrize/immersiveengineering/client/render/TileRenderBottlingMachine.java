package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.obj.Vertex;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelIEObj;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.BlockMetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBottlingMachine;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public class TileRenderBottlingMachine extends TileRenderIE
{
	ModelIEObj model = new ModelIEObj("immersiveengineering:models/bottlingMachine.obj")
	{
		@Override
		public IIcon getBlockIcon(String groupName)
		{
			if(groupName.equalsIgnoreCase("conveyors"))
				return IEContent.blockMetalDevice.getIcon(0, BlockMetalDevices.META_conveyorBelt);
			return IEContent.blockMetalMultiblocks.getIcon(0, BlockMetalMultiblocks.META_bottlingMachine);
		}
	};

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityBottlingMachine bottler = (TileEntityBottlingMachine)tile;

		translationMatrix.translate(.5, 0, .5);
		rotationMatrix.rotate(Math.toRadians(bottler.facing==3?180: bottler.facing==4?90: bottler.facing==5?-90: 0), 0,1,0);
		if(bottler.mirrored)
			translationMatrix.scale(new Vertex(bottler.facing<4?-1:1,1,bottler.facing>3?-1:1));

		model.render(tile, tes, translationMatrix, rotationMatrix, 0, bottler.mirrored, "conveyors","base");
	}
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityBottlingMachine bottler = (TileEntityBottlingMachine)tile;
		if(!bottler.formed || bottler.pos!=4)
			return;
		GL11.glPushMatrix();
		GL11.glTranslated(x+.5, y, z+.5);
		GL11.glRotated(bottler.facing==3?180: bottler.facing==4?90: bottler.facing==5?-90: 0, 0,1,0);
		double shift = .3358;

		Matrix4 translationMatrix = new Matrix4();
		Matrix4 rotationMatrix = new Matrix4();
		if(bottler.mirrored)
			GL11.glScalef(-1,1,1);

		GL11.glTranslated(shift/2,0,0);

		double d0 = .05867077;
		double d1 = .08265846;
		double tapShift = 0;

		for(int i=0; i<bottler.inventory.length; i++)
			if(bottler.inventory[i]!=null && bottler.process[i]>0)
			{
				float step = bottler.process[i]/120f;
				double fill = step>=.4+d0+d1*2?1:0;

				if(step>=.4+d0 && step<.4+d0+d1)
					fill = tapShift = (step-.4-d0)/d1;
				else if(step>=.4+d0+d1 && step<.4+d0+d1*2)
					fill = tapShift = 1;
				else if(step>=.4+d0+d1 && step<.4+d0+d1*3)
					tapShift = 1-(step-.4-d0-d1*2)/d1;

				GL11.glPushMatrix();
				GL11.glTranslated(1,1.15625,.5);
				double itemX = 0;
				double itemY = 0;
				double itemZ = 0;
				itemX = -( step<.18?0: step<.4?((step-.18)/.22)*.75: step<.6?.75+((step-.40)/.2)*.8125: step<.82?1.5625+(step-.6)/.22*.75: 2.3125);
				itemZ = -( step<.18?step/.18*.9: step<.4?.9+((step-.18)/.22)*.7875: step<.6? 1.6875: step<.82?1.6875-((step-.6)/.22)*.7875: (1-step)/.18 * .9);

				GL11.glTranslated(itemX,itemY,itemZ);

				if(bottler.mirrored)
					GL11.glScalef(-1,1,1);
				renderItemToFill(bottler.inventory[i], bottler.getFilledItem(bottler.inventory[i],false), (float)fill, step>=.71, bottler.getWorldObj());
				GL11.glPopMatrix();
			}

		GL11.glTranslated(-shift*tapShift,0,0);
		Tessellator tes = ClientUtils.tes();
		ClientUtils.bindAtlas(0);
		tes.startDrawingQuads();
		model.render(tile, tes, translationMatrix,rotationMatrix, 0, bottler.mirrored, "tap");
		tes.draw();
		GL11.glTranslated(shift*tapShift,0,0);

		GL11.glPopMatrix();
	}

	static void renderItemToFill(ItemStack empty, ItemStack full, float fill, boolean packaged, World world)
	{
		if(empty==null||full==null)
			return;
		IItemRenderer iirEmpty = MinecraftForgeClient.getItemRenderer(empty, ItemRenderType.ENTITY);
		IItemRenderer iirFull = MinecraftForgeClient.getItemRenderer(full, ItemRenderType.ENTITY);
		if(iirEmpty==null && iirFull==null && empty.getItemSpriteNumber()==1 && full.getItemSpriteNumber()==1)
		{
			GL11.glPushMatrix();
			ClientUtils.bindAtlas(1);
			GL11.glTranslated(.0,-.0525,.0625/6);
			GL11.glScalef(.51f, .51f, .51f);

			if(fill>0)
				for(int pass=0; pass<full.getItem().getRenderPasses(full.getItemDamage()); pass++)
				{
					IIcon iconFull = full.getItem().getIcon(full, pass);
					int h = Math.round(fill*iconFull.getIconHeight());
					double pxFill = h/(double)iconFull.getIconHeight();
					int col = full.getItem().getColorFromItemStack(full, pass);
					GL11.glColor3f((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f);
					ClientUtils.renderItemIn2D(iconFull, new double[]{0,1,1-pxFill,1}, iconFull.getIconWidth(),h, .0625f);
					GL11.glColor3f(1,1,1);
				}
			if(fill<1)
				for(int pass=0; pass<empty.getItem().getRenderPasses(empty.getItemDamage()); pass++)
				{
					IIcon iconEmpty = empty.getItem().getIcon(empty, pass);
					int h = Math.round((1-fill)*iconEmpty.getIconHeight());
					double pxFill = h/(double)iconEmpty.getIconHeight();
					GL11.glTranslated(0,1-pxFill,0);
					int col = empty.getItem().getColorFromItemStack(empty, pass);
					GL11.glColor3f((col>>16&255)/255f, (col>>8&255)/255f, (col&255)/255f);
					ClientUtils.renderItemIn2D(iconEmpty, new double[]{0,1,0,pxFill}, iconEmpty.getIconWidth(),h, .0625f);
					GL11.glColor3f(1,1,1);
					GL11.glTranslated(0,-1-pxFill,0);
				}
			GL11.glPopMatrix();
		}
		else
		{
			EntityItem entityitem = new EntityItem(world, 0.0D, 0.0D, 0.0D, packaged?full:empty);
			entityitem.getEntityItem().stackSize = 1;
			entityitem.hoverStart = 0.0F;
			RenderItem.renderInFrame = true;
			RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
			RenderItem.renderInFrame = false;
		}


	}
}