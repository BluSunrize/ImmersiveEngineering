package blusunrize.immersiveengineering.client.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCaseMinecart;
import blusunrize.immersiveengineering.client.ClientUtils;

public class ModelShaderMinecart extends ModelMinecart
{
	public static HashMap<Integer, ItemStack> shadedCarts = new HashMap<Integer, ItemStack>();
	public static boolean rendersReplaced = false;

	//	public ModelMinecart wrappedModel;
	//	public ModelMinecart wrappedMirroredModel;
	public ModelRenderer[] sideModelsMirrored = new ModelRenderer[7];

	public ModelShaderMinecart(ModelMinecart model)
	{
		super();
		this.sideModels = ClientUtils.copyModelRenderers(model, model.sideModels);
		this.sideModelsMirrored = ClientUtils.copyModelRenderers(model, model.sideModels);
		sideModelsMirrored[4].mirror = true;
		ArrayList<ModelBox> newCubes = new ArrayList<ModelBox>();
		for(ModelBox cube :  (List<ModelBox>)sideModelsMirrored[4].cubeList)
			newCubes.add(new ModelBox(sideModelsMirrored[4], 0,0, cube.posX1,cube.posY1,cube.posZ1, (int)(cube.posX2-cube.posX1),(int)(cube.posY2-cube.posY1),(int)(cube.posZ2-cube.posZ1), 0));
		sideModelsMirrored[4].cubeList = newCubes;
	}


	@Override
	public void render(Entity entity, float f0, float f1, float f2, float f3, float f4, float f5)
	{
		ShaderCase sCase = null;
		ItemStack shader = null;
		if(shadedCarts.containsKey(entity.getEntityId()))
		{
			shader = shadedCarts.get(entity.getEntityId());
			if(shader!=null && shader.getItem() instanceof IShaderItem)
				sCase = ((IShaderItem)shader.getItem()).getShaderCase(shader,null,"minecart");
		}
		if(sCase!=null && sCase instanceof ShaderCaseMinecart)
		{
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 0, 1);

			sideModels[5].rotationPointY = 4.0F - f2;
			sideModelsMirrored[5].rotationPointY = 4.0F - f2;
			for(int part=0;part<sideModels.length-1;part++)
				if(sideModels[part]!=null)
				{
					float scale = 1;
					int maxPasses = sCase.getPasses(shader,null,""+part);
					for(int pass=0; pass<maxPasses; pass++)
						if( (((ShaderCaseMinecart)sCase).additionalTexture!=null&&pass==2)?(part!=1&&part!=2): (pass!=1 || ((ShaderCaseMinecart)sCase).overlaySides[part]))
						{
							int[] col = sCase.getRGBAColourModifier(shader, null, ""+part, pass);
							GL11.glScalef(scale,scale,scale);
							GL11.glColor4f(col[0]/255f,col[1]/255f,col[2]/255f,col.length>3?(col[3]/255f):1f);

							String texture = pass==0?"0": pass==1?("1_"+((ShaderCaseMinecart)sCase).getOverlayType()): "uncoloured";
							if(pass==2 && ((ShaderCaseMinecart)sCase).additionalTexture!=null)
								texture = ((ShaderCaseMinecart)sCase).additionalTexture;
							ClientUtils.bindTexture("immersiveengineering:textures/models/shaders/minecart_"+texture+".png");

							if(((ShaderCaseMinecart)sCase).mirrorSideForPass[pass])
								sideModelsMirrored[part].render(f5);
							else
								sideModels[part].render(f5);

							GL11.glColor4f(1,1,1,1);
							GL11.glScalef(1/scale,1/scale,1/scale);
							scale += .001f;
						}
				}

			GL11.glDisable(GL11.GL_BLEND);
		}
		else
			super.render(entity, f0, f1, f2, f3, f4, f5);
	}
}