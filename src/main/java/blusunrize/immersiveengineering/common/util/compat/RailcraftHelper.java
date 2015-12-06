package blusunrize.immersiveengineering.common.util.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelMinecart;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import blusunrize.immersiveengineering.api.shader.ShaderCaseMinecart;
import blusunrize.immersiveengineering.client.models.ModelShaderMinecart;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class RailcraftHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
	}

	@Override
	public void postInit()
	{
		if(FMLCommonHandler.instance().getEffectiveSide()==Side.CLIENT)
		{
			try{
				Class c_CartModelManager = Class.forName("mods.railcraft.client.render.carts.CartModelManager");
				Field f_modelMinecart = c_CartModelManager.getDeclaredField("modelMinecart");
				f_modelMinecart.setAccessible(true);
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.set(f_modelMinecart, f_modelMinecart.getModifiers() & ~Modifier.FINAL);
				ModelMinecart wrapped = (ModelMinecart)f_modelMinecart.get(null);
				f_modelMinecart.set(null, new ModelShaderMinecart(wrapped));
				modifiersField.set(f_modelMinecart, f_modelMinecart.getModifiers() | Modifier.FINAL);

				Field f_modelsCore = c_CartModelManager.getDeclaredField("modelsCore");
				Map<Class, ModelBase> modelMap = (Map<Class, ModelBase>)f_modelsCore.get(null);
				for(Map.Entry<Class, ModelBase> e : modelMap.entrySet())
					if(e.getValue().getClass().getName().endsWith("ModelLowSidesMinecart"))
						e.setValue(new ModelShaderLowSidesMinecart());
				
				ShaderCaseMinecart.invalidMinecartClasses.add((Class<? extends EntityMinecart>)Class.forName("mods.railcraft.common.carts.EntityLocomotive"));
				ShaderCaseMinecart.invalidMinecartClasses.add((Class<? extends EntityMinecart>)Class.forName("mods.railcraft.common.carts.EntityTunnelBore"));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}


	@SideOnly(Side.CLIENT)
	public class ModelShaderLowSidesMinecart extends ModelShaderMinecart
	{
		public ModelShaderLowSidesMinecart()
		{
			super(new ModelMinecart());
			byte length = 20;
			byte width = 16;
			byte yOffset = 4;
			byte height = 6;
			this.wrappedModel.sideModels[3] = new ModelRenderer(this, 0, 0);
			this.wrappedModel.sideModels[4] = new ModelRenderer(this, 0, 0);
			this.wrappedModel.sideModels[4].mirror = true;
		    this.wrappedModel.sideModels[3].addBox(-length/2+2, -height-1, -1, length-4, height, 2, 0);
		    this.wrappedModel.sideModels[3].setRotationPoint(0, yOffset, -width/2+1);
		    this.wrappedModel.sideModels[4].addBox(-length/2+2, -height-1, -1, length-4, height, 2, 0);
		    this.wrappedModel.sideModels[4].setRotationPoint(0, yOffset, width/2-1);
			this.wrappedModel.sideModels[3].rotateAngleY = (float)Math.PI;
			

		}
		

		@Override
		public void render(Entity entity, float f0, float f1, float f2, float f3, float f4, float f5)
		{
			super.render(entity, f0, f1, f2, f3, f4, f5);
		}
	}
}