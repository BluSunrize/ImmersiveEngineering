package blusunrize.immersiveengineering.common.util.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.shader.ShaderCaseChemthrower;
import blusunrize.immersiveengineering.api.shader.ShaderCaseDrill;
import blusunrize.immersiveengineering.api.shader.ShaderCaseMinecart;
import blusunrize.immersiveengineering.api.shader.ShaderCaseRailgun;
import blusunrize.immersiveengineering.api.shader.ShaderCaseRevolver;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.client.ClientEventHandler;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.client.ClientUtils;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class AvaritiaHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void init()
	{
		EnumRarity cosmicRarity = EnumRarity.valueOf("COSMIC");
		ShaderRegistry.rarityWeightMap.put(cosmicRarity,0);

		String name = "Cosmic";
		String overlayType = "4";
		int[] colourPrimary = {8,8,10,255};
		int[] colourSecondary = {255,255,255,255};
		int[] colourBackground = {31,30,37,255};
		int[] colourBlade = {255,255,255,255};
		ShaderRegistry.shaderList.add(name);
		ShaderRegistry.shaderRegistry.put(name, new ShaderRegistry.ShaderRegistryEntry(
				name,
				cosmicRarity,
				new CosmicShaderCaseRevolver(overlayType, colourBackground, colourPrimary, colourSecondary, colourBlade, null),
				new CosmicShaderCaseDrill(overlayType, colourBackground, colourPrimary, colourSecondary, null),
				new CosmicShaderCaseChemthrower(overlayType, colourBackground, colourPrimary, colourSecondary, true,false, null),
				new CosmicShaderCaseRailgun(overlayType, colourBackground, colourPrimary, colourSecondary, null),
				new CosmicShaderCaseMinecart(overlayType, colourBackground, colourPrimary, colourSecondary, null)).setBagLoot(true).setInLowerBags(false));
	}
	@Override
	public void postInit()
	{
		EnumRarity trash = EnumRarity.valueOf("TRASH");
		if(trash!=null)
			ShaderRegistry.rarityWeightMap.put(trash,11);
		
		if(Loader.isModLoaded("TConstruct"))
		{
			Item itemToolrod = GameRegistry.findItem("TConstruct", "toolRod");
			if(itemToolrod!=null)
			{
				RailgunHandler.registerProjectileProperties(new ItemStack(itemToolrod,1,500), 20,20).setColourMap(new int[][]{{0xa4a8c0,0x0e0e0e,0x0e0e0e,0x0e0e0e,0x0e0e0e,0x1c1c1c,0x1c1c1c,0xa4a8c0}});
				RailgunHandler.registerProjectileProperties(new ItemStack(itemToolrod,1,501), 800,2).setColourMap(new int[][]{{0xe64452,0xe64452,0xe64452, 0xff425f,0xff425f},{0xd85085,0xd85085,0xd85085, 0xce41b2,0xce41b2},{0xec64f1,0xec64f1,0xec64f1, 0xf255f9,0xf255f9},{0xb146ff,0xb146ff,0xb146ff, 0x522ac8,0x522ac8},{0x7c74ee,0x7c74ee,0x7c74ee, 0x4a4ad5,0x4a4ad5},{0x6dc6f1,0x6dc6f1,0x6dc6f1, 0x57bdd9,0x57bdd9},{0x70d0a4,0x70d0a4,0x70d0a4, 0x74af93,0x74af93},{0x70c354,0x70c354,0x70c354, 0x34aa34,0x34aa34},{0x7de026,0x7de026,0x7de026, 0x219c21,0x219c21},{0xbcec13,0xbcec13,0xbcec13, 0x779202,0x779202},{0xedd02d,0xedd02d,0xedd02d, 0x928509,0x928509},{0xef725a,0xef725a,0xef725a, 0xef5739,0xef5739}});
			}
		}
	}

	static Field f_z_inventoryRender;
	static Field f_f_cosmicOpacity;
	static Method m_useShader;
	static Method m_releaseShader;

	public static void applyCosmicShader(boolean pre, boolean inventory)
	{
		if(f_z_inventoryRender==null)
		{
			try{
				Class c_CosmicRenderShenanigans = Class.forName("fox.spiteful.avaritia.render.CosmicRenderShenanigans");
				f_z_inventoryRender = c_CosmicRenderShenanigans.getDeclaredField("inventoryRender");
				f_f_cosmicOpacity = c_CosmicRenderShenanigans.getDeclaredField("cosmicOpacity");
				m_useShader = c_CosmicRenderShenanigans.getDeclaredMethod("useShader");
				m_releaseShader = c_CosmicRenderShenanigans.getDeclaredMethod("releaseShader");
			}catch(Exception e){e.printStackTrace();}
		}
		try{

			if(pre)
			{
				f_z_inventoryRender.setBoolean(null, inventory);
				m_useShader.invoke(null);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glDepthFunc(GL11.GL_EQUAL);
			}
			else
			{
				GL11.glDepthFunc(GL11.GL_LEQUAL);
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				m_releaseShader.invoke(null);
				f_z_inventoryRender.setBoolean(null, false);
			}
		}catch(Exception e){}
	}

	static class CosmicShaderCaseChemthrower extends ShaderCaseChemthrower
	{
		public CosmicShaderCaseChemthrower(String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, boolean cageOnBase, boolean tanksUncoloured, String additionalTexture)
		{
			super(overlayType, colourGrip, colourPrimary, colourSecondary, cageOnBase, tanksUncoloured, additionalTexture);
		}

		@Override
		public int getPasses(ItemStack shader, ItemStack item, String modelPart)
		{
			if(modelPart.equals("tanks"))
				return (tanksUncoloured?1:2)+(additionalTexture!=null?1:0);
			return super.getPasses(shader, item, modelPart)+1;
		}
		@Override
		public int getTextureType(String modelPart, int pass)
		{
			int i = super.getTextureType(modelPart, pass);
			return i==3?2:i;
		}
		@Override
		public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
		{
			if(pass==1 && pre)
			{
				GL11.glScalef(1.001f,1.001f,1.001f);
				GL11.glTranslatef(-.0005f,0,0);
			}
			if(pass==2)
				applyCosmicShader(pre, inventory);
		}
	}
	static class CosmicShaderCaseDrill extends ShaderCaseDrill
	{
		public CosmicShaderCaseDrill(String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
		{
			super(overlayType, colourGrip, colourPrimary, colourSecondary, additionalTexture);
		}

		@Override
		public int getPasses(ItemStack shader, ItemStack item, String modelPart)
		{
			int i = super.getPasses(shader, item, modelPart);
			return i>2?i+1:i;
		}
		@Override
		public int getTextureType(String modelPart, int pass)
		{
			int i = super.getTextureType(modelPart, pass);
			return i==3?2:i;
		}
		@Override
		public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
		{
			if(pass==1 && pre)
			{
				GL11.glScalef(1.001f,1.001f,1.001f);
				GL11.glTranslatef(-.0005f,0,0);
			}
			if(pass==2)
				applyCosmicShader(pre, inventory);
		}
	}
	static class CosmicShaderCaseRailgun extends ShaderCaseRailgun
	{
		public CosmicShaderCaseRailgun(String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
		{
			super(overlayType, colourGrip, colourPrimary, colourSecondary, additionalTexture);
		}

		@Override
		public int getPasses(ItemStack shader, ItemStack item, String modelPart)
		{
			if(modelPart.equals("sled")||modelPart.equals("wires"))
				return 1;
			int i = super.getPasses(shader, item, modelPart);
			return i+1;
		}
		@Override
		public int getTextureType(String modelPart, int pass)
		{
			int i = super.getTextureType(modelPart, pass);
			return i==3?2:i;
		}
		@Override
		public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
		{
			if(pass==1 && pre)
				GL11.glScalef(1.001f,1.001f,1.001f);
			if(pass==2)
				applyCosmicShader(pre, inventory);
		}
	}
	static class CosmicShaderCaseMinecart extends ShaderCaseMinecart
	{
		public CosmicShaderCaseMinecart(String overlayType, int[] colourUnderlying, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
		{
			super(overlayType, colourUnderlying, colourPrimary, colourSecondary, additionalTexture);
		}
		@Override
		public int getPasses(ItemStack shader, ItemStack item, String modelPart)
		{
			return super.getPasses(shader, item, modelPart)+1;
		}
		@Override
		public IIcon getReplacementIcon(ItemStack shader, ItemStack item, String modelPart, int pass)
		{
			if(pass==2)
				return ClientEventHandler.iconItemBlank;
			return null;
		}
		@Override
		public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
		{
			if(pass==1 && pre)
				GL11.glScalef(1.0001f,1.0001f,1.0001f);
			if(pass==2)
			{
				ClientUtils.bindAtlas(1);
				applyCosmicShader(pre, inventory);
			}
		}
	}
	static class CosmicShaderCaseRevolver extends ShaderCaseRevolver
	{
		public CosmicShaderCaseRevolver(String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, int[] colourBlade, String additionalTexture)
		{
			super(overlayType, colourGrip, colourPrimary, colourSecondary, colourBlade, additionalTexture);
		}

		@Override
		public int getPasses(ItemStack shader, ItemStack item, String modelPart)
		{
			return super.getPasses(shader, item, modelPart)+1;
		}
		@Override
		public int getTextureType(String modelPart, int pass)
		{
			int i = super.getTextureType(modelPart, pass);
			return i>2?i-1:i;
		}
		@Override
		public IIcon getReplacementIcon(ItemStack shader, ItemStack item, String modelPart, int pass)
		{
			IIcon icon = super.getReplacementIcon(shader, item, modelPart, pass);
			if(modelPart.equals("revolver_frame"))
				pass--;
			if(modelPart.equals("player_bayonet")||modelPart.equals("dev_bayonet") || modelPart.equals("player_mag")||modelPart.equals("dev_mag"))
				pass++;
			if(pass==3)
				return ClientEventHandler.iconItemBlank;
			return icon;
		}
		@Override
		public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
		{
			if(modelPart.equals("revolver_frame"))
				pass--;
			if(modelPart.equals("player_bayonet")||modelPart.equals("dev_bayonet") || modelPart.equals("player_mag")||modelPart.equals("dev_mag"))
				pass++;
			if(pass==1 && pre)
			{
				GL11.glScalef(1.002f,1.002f,1.002f);
				GL11.glTranslatef(0,-.0005f,0);
			}
			if(pass==3)
			{
				ClientUtils.bindAtlas(1);
				applyCosmicShader(pre, inventory);
				if(!pre)
					ClientUtils.mc().renderEngine.bindTexture(ClientProxy.revolverTextureResource);
			}
		}
	}
}