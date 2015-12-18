package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiArcFurnace;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class NEIArcFurnaceHandler extends TemplateRecipeHandler
{
	public String specialRecipeType;

	public NEIArcFurnaceHandler()
	{
		String name = this.getClass().getName();
		int idx = name.indexOf("NEIArcFurnaceHandler");
		if(idx>=0 && idx+"NEIArcFurnaceHandler".length()<name.length())
			specialRecipeType = name.substring(idx+"NEIArcFurnaceHandler".length());
	}

	public static NEIArcFurnaceHandler createSubHandler(String subtype)
	{
		try{
			String entitySuperClassName = Type.getInternalName(NEIArcFurnaceHandler.class);
			String entityProxySubClassName = NEIArcFurnaceHandler.class.getSimpleName().concat(subtype);

			ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			cw.visit(Opcodes.V1_6,Opcodes.ACC_PUBLIC+Opcodes.ACC_SUPER,entityProxySubClassName,null,entitySuperClassName,null);
			cw.visitSource(entityProxySubClassName.concat(".java"),null);

			//create constructor
			MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,"<init>","()V",null,null);
			mv.visitCode();
			mv.visitVarInsn(Opcodes.ALOAD,0);
			mv.visitMethodInsn(Opcodes.INVOKESPECIAL,entitySuperClassName,"<init>","()V", false);
			mv.visitInsn(Opcodes.RETURN);
			mv.visitMaxs(0,0);
			mv.visitEnd();
			cw.visitEnd();

			Class<? extends NEIArcFurnaceHandler> c = (Class<? extends NEIArcFurnaceHandler>) new ProxyClassLoader(Thread.currentThread().getContextClassLoader(),cw.toByteArray()).loadClass(entityProxySubClassName.replaceAll("/","."));
			return c.newInstance();
		}catch(Exception e)
		{
		}
		return null;
	}
	public static class ProxyClassLoader extends ClassLoader
	{
		private byte[] rawClassBytes;
		public ProxyClassLoader(ClassLoader parentClassLoader,byte[] classBytes)
		{
			super(parentClassLoader);
			this.rawClassBytes = classBytes;
		}
		@Override
		public Class findClass(String name)
		{
			return defineClass(name,this.rawClassBytes, 0,this.rawClassBytes.length);
		}
	}


	public class CachedArcFurnaceRecipe extends CachedRecipe
	{
		PositionedStack[] inputs;
		PositionedStack[] output;
		PositionedStack slag;
		public int time;
		public int energy;
		public CachedArcFurnaceRecipe(ArcFurnaceRecipe recipe)
		{
			ArrayList<PositionedStack> lInputs = new ArrayList<PositionedStack>();
			ItemStack[] stackAdditives = new ItemStack[recipe.additives.length]; 
			if(recipe.input!=null)
				lInputs.add(new PositionedStack(recipe.input, 28, 0));
			for(int i=0; i<recipe.additives.length; i++)
				if(recipe.additives[i]!=null)
				{
					lInputs.add(new PositionedStack(recipe.additives[i], 20+i%2*18, 24+i/2*18));
					stackAdditives[i] = ApiUtils.getItemStackFromObject(recipe.additives[i]);
				}
			inputs = lInputs.toArray(new PositionedStack[lInputs.size()]);


			ItemStack[] outs = recipe.getOutputs(ApiUtils.getItemStackFromObject(recipe.input), stackAdditives);
			if(outs!=null&&outs.length>0)
			{
				output = new PositionedStack[outs.length];
				int l = output.length;
				for(int i=0; i<l; i++)
					output[i] = new PositionedStack(outs[i], 122+i%2*18, (i>2?0:16)+i/2*18);
			}
			else if(recipe.output!=null)
				output = new PositionedStack[]{new PositionedStack(recipe.output, 122,16)};
			if(recipe.slag!=null)
				slag = new PositionedStack(recipe.slag, 122,36);
			time = recipe.time;
			energy = recipe.energyPerTick;
		}
		@Override
		public List<PositionedStack> getOtherStacks()
		{
			ArrayList<PositionedStack> l = new ArrayList<PositionedStack>();
			if(slag!=null)
				l.add(slag);
			if(output!=null)
				for(int i=1; i<output.length; i++)
					l.add(output[i]);
			return l;
		}
		@Override
		public List<PositionedStack> getIngredients()
		{
			return getCycledIngredients(cycleticks/20, Arrays.asList(inputs));
		}
		@Override
		public PositionedStack getResult()
		{
			return output!=null?output[0]:null;
		}
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiArcFurnace.class;
	}

	NEIArcFurnaceHandler setSpecialType(String type)
	{
		this.specialRecipeType = type;
		return this;
	}
	boolean specialTypeCompare(ArcFurnaceRecipe r)
	{
		if((this.specialRecipeType==null||this.specialRecipeType.isEmpty()) && (r.specialRecipeType==null||r.specialRecipeType.isEmpty()))
			return true;
		if(this.specialRecipeType!=null&&r.specialRecipeType!=null&&this.specialRecipeType.equalsIgnoreCase(r.specialRecipeType))
			return true;
		return false;
	}

	@Override
	public void loadTransferRects()
	{
		transferRects.add(new RecipeTransferRect(new Rectangle(76,26, 32,40), "ieArcFurnace"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(getOverlayIdentifier().startsWith(outputId))
		{
			for(ArcFurnaceRecipe r : ArcFurnaceRecipe.recipeList)
				if(r!=null && r.input!=null && specialTypeCompare(r))
					this.arecipes.add(new CachedArcFurnaceRecipe(r));
		}
		else
			super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.metalMultiblock.arcFurnace.name")+(this.specialRecipeType!=null&&!this.specialRecipeType.isEmpty()?(": "+this.specialRecipeType):"");
	}
	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/arcFurnace.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieArcFurnace";
	}
	@Override
	public int recipiesPerPage()
	{
		return 2;
	}
	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		if(result!=null)
			for(ArcFurnaceRecipe r : ArcFurnaceRecipe.recipeList)
				if(r!=null && r.input!=null && (Utils.stackMatchesObject(result, r.output)||(r.slag!=null&&Utils.stackMatchesObject(result, r.slag))) && specialTypeCompare(r))
					this.arecipes.add(new CachedArcFurnaceRecipe(r));
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
			for(ArcFurnaceRecipe r : ArcFurnaceRecipe.recipeList)
				if(r!=null && r.input!=null && specialTypeCompare(r))
				{
					if(Utils.stackMatchesObject(ingredient, r.input))
						this.arecipes.add(new CachedArcFurnaceRecipe(r));
					else
						for(Object o : r.additives)
							if(Utils.stackMatchesObject(ingredient, o))
								this.arecipes.add(new CachedArcFurnaceRecipe(r));
				}
	}

	@Override
	public void drawBackground(int recipe)
	{
		GL11.glPushMatrix();
		GL11.glColor4f(1, 1, 1, 1);

		CachedArcFurnaceRecipe r = (CachedArcFurnaceRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			ClientUtils.drawSlot(28, 0, 16, 16);
			for(int i=0; i<4; i++)
				ClientUtils.drawSlot(20+i%2*18, 24+i/2*18, 16, 16);
			if(r.output!=null)
				for(PositionedStack ps : r.output)
					ClientUtils.drawSlot(ps.relx, ps.rely, 16, 16);
			if(r.slag!=null)
				ClientUtils.drawSlot(r.slag.relx, r.slag.rely, 16, 16);

			String s = r.energy+" RF/t";
			ClientUtils.font().drawString(s, 88-ClientUtils.font().getStringWidth(s)/2,32, 0x777777, false);
			s = r.time+" ticks";
			ClientUtils.font().drawString(s, 84-ClientUtils.font().getStringWidth(s)/2,44, 0x777777, false);
			GL11.glColor4f(1, 1, 1, 1);
			changeTexture("textures/gui/container/furnace.png");
			drawTexturedModalRect(72,16, 80,35, 22,16);
			int w = (this.cycleticks/2)%22;
			drawTexturedModalRect(72,16, 177,14, w,16);
		}
		GL11.glPopMatrix();
	}

}