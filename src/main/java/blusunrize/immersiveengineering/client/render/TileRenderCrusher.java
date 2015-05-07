package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;

public class TileRenderCrusher extends TileEntitySpecialRenderer
{
	static ModelCrusher model = new ModelCrusher();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityCrusher crusher = (TileEntityCrusher)tile;
		if(!crusher.formed || crusher.pos!=17)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		GL11.glTranslated(+.5, +1.5, +.5);
		GL11.glScalef(1.0F, -1.0F, -1.0F);

		model.base.rotateAngleY=(float) Math.toRadians(crusher.facing==2?180: crusher.facing==4?90: crusher.facing==5?-90: 0);

		ClientUtils.bindTexture("immersiveengineering:textures/models/crusher.png");

		model.axle1.rotateAngleX = (float) Math.toRadians(crusher.barrelRotation + (crusher.barrelRotation == 0? 0 :  f));
		model.axle2.rotateAngleX = (float) Math.toRadians(-crusher.barrelRotation - (crusher.barrelRotation == 0? 0 :  f));

		model.render(null, 0, 0, 0, 0, 0, .0625f);
		GL11.glTranslated(0,-1,0);
		GL11.glScalef(.75F, -.75F, -.75F);

//		int i=0;
////		System.out.println(crusher.inputs.size());
//		for(ItemStack stack : crusher.inputs)
//			if(stack!=null)
//			{
//				GL11.glTranslated(.1*(i%4)*((i*3)%2==0?1:-1),  0,  i*.33*(i%2==0?1:-1));
//				double yOff=0;
//				if(i==0)
//				{
//					CrusherRecipe r = CrusherRecipe.findRecipe(stack);
//					if(r!=null)
//						yOff= .5*(crusher.process/(float)r.energy);
//				}
////				if(yOff!=0)
////					GL11.glTranslated(0,-yOff,0);
//				EntityItem item = new EntityItem(tile.getWorldObj(), 0.0D, 0.0D, 0.0D, stack);
//				item.hoverStart = 0.0F;
//				RenderItem.renderInFrame=true;
//				RenderManager.instance.renderEntityWithPosYaw(item, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
//				RenderItem.renderInFrame=false;
////				if(yOff!=0)
////					GL11.glTranslated(0, yOff,0);
//				i++;
//			}

		GL11.glPopMatrix();
	}

}