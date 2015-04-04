package blusunrize.immersiveengineering.client;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.client.gui.GuiBlastFurnace;
import blusunrize.immersiveengineering.client.gui.GuiCokeOven;
import blusunrize.immersiveengineering.client.gui.GuiRevolver;
import blusunrize.immersiveengineering.client.gui.manual.GuiManual;
import blusunrize.immersiveengineering.client.gui.manual.ManualPages;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDecoration;
import blusunrize.immersiveengineering.client.render.BlockRenderMetalDevices;
import blusunrize.immersiveengineering.client.render.BlockRenderStoneDevices;
import blusunrize.immersiveengineering.client.render.BlockRenderWoodenDecoration;
import blusunrize.immersiveengineering.client.render.BlockRenderWoodenDevices;
import blusunrize.immersiveengineering.client.render.EntityRenderRevolvershot;
import blusunrize.immersiveengineering.client.render.ItemRenderRevolver;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorHV;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorLV;
import blusunrize.immersiveengineering.client.render.TileRenderConnectorMV;
import blusunrize.immersiveengineering.client.render.TileRenderPost;
import blusunrize.immersiveengineering.client.render.TileRenderRelayHV;
import blusunrize.immersiveengineering.client.render.TileRenderTransformer;
import blusunrize.immersiveengineering.client.render.TileRenderWatermill;
import blusunrize.immersiveengineering.client.render.TileRenderWindmill;
import blusunrize.immersiveengineering.client.render.TileRenderWindmillAdvanced;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.Lib;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRelayHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformer;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTransformerHV;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityBlastFurnace;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWoodenPost;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import blusunrize.immersiveengineering.common.items.ItemRevolver;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{

	@Override
	public void init()
	{
		//METAL
		RenderingRegistry.registerBlockHandler(new BlockRenderMetalDevices());
		RenderingRegistry.registerBlockHandler(new BlockRenderMetalDecoration());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorLV.class, new TileRenderConnectorLV());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorMV.class, new TileRenderConnectorMV());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransformer.class, new TileRenderTransformer(false));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRelayHV.class, new TileRenderRelayHV());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityConnectorHV.class, new TileRenderConnectorHV());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTransformerHV.class, new TileRenderTransformer(true));
		//WOOD
		RenderingRegistry.registerBlockHandler(new BlockRenderWoodenDevices());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWoodenPost.class, new TileRenderPost());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWatermill.class, new TileRenderWatermill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindmill.class, new TileRenderWindmill());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindmillAdvanced.class, new TileRenderWindmillAdvanced());

		RenderingRegistry.registerBlockHandler(new BlockRenderWoodenDecoration());
		//STONE
		RenderingRegistry.registerBlockHandler(new BlockRenderStoneDevices());
		
		//REVOLVER
		MinecraftForgeClient.registerItemRenderer(IEContent.itemRevolver, new ItemRenderRevolver());
		RenderingRegistry.registerEntityRenderingHandler(EntityRevolvershot.class, new EntityRenderRevolvershot());

		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
	}

	@Override
	public void serverStart()
	{
		GuiManual.manualContents.clear();
		GuiManual.addEntry("introduction", new ManualPages.Text("introduction0"),new ManualPages.Text("introduction1"),new ManualPages.Crafting("introductionHammer", new ItemStack(IEContent.itemTool,1,0)));
		GuiManual.addEntry("ores", 
				new ManualPages.Items("oresCopper", new ItemStack(IEContent.blockOres,1,0),new ItemStack(IEContent.itemMetal,1,0)),
				new ManualPages.Items("oresBauxite", new ItemStack(IEContent.blockOres,1,1),new ItemStack(IEContent.itemMetal,1,1)),
				new ManualPages.Items("oresLead", new ItemStack(IEContent.blockOres,1,2),new ItemStack(IEContent.itemMetal,1,2)),
				new ManualPages.Items("oresSilver", new ItemStack(IEContent.blockOres,1,3),new ItemStack(IEContent.itemMetal,1,3)),
				new ManualPages.Items("oresNickel", new ItemStack(IEContent.blockOres,1,4),new ItemStack(IEContent.itemMetal,1,4)));
		GuiManual.addEntry("cokeoven", new ManualPages.Text("cokeoven0"), new ManualPages.Crafting("cokeovenBlock", new ItemStack(IEContent.blockStoneDevice,1,1)));
		GuiManual.addEntry("treatedwood", new ManualPages.Text("treatedwood0"), 
				new ManualPages.Crafting("", new ItemStack(IEContent.blockWoodenDecoration,1,0),new ItemStack(IEContent.blockWoodenDecoration,1,2),new ItemStack(IEContent.blockWoodenStair)),
				new ManualPages.Crafting("", new ItemStack(IEContent.itemMaterial,1,0),new ItemStack(IEContent.blockWoodenDecoration,1,1)),
				new ManualPages.Crafting("treatedwoodPost0", new ItemStack(IEContent.blockWoodenDevice,1,0)),
				new ManualPages.Text("treatedwoodPost1"));
		GuiManual.addEntry("wiring", new ManualPages.Text("wiring0"), 
				new ManualPages.Crafting("wiring1", new ItemStack(IEContent.itemWireCoil,1,OreDictionary.WILDCARD_VALUE)),
				new ManualPages.Image("wiring2", "immersiveengineering:textures/misc/wiring.png;0;0;110;40", "immersiveengineering:textures/misc/wiring.png;0;40;110;30"),
				new ManualPages.Image("wiring3", "immersiveengineering:textures/misc/wiring.png;0;70;110;60", "immersiveengineering:textures/misc/wiring.png;0;130;110;55"),
				new ManualPages.Crafting("", new ItemStack(IEContent.blockMetalDevice,1,0),new ItemStack(IEContent.blockMetalDevice,1,3)),
				new ManualPages.Crafting("wiringCapacitor", new ItemStack(IEContent.blockMetalDevice,1,1)),
				new ManualPages.Crafting("wiringCutters", new ItemStack(IEContent.itemTool,1,1)),
				new ManualPages.Crafting("wiringVoltmeter", new ItemStack(IEContent.itemTool,1,2)));
		GuiManual.addEntry("generator",
				new ManualPages.Text("generator0"),
				new ManualPages.Crafting("", new ItemStack(IEContent.blockMetalDevice,1,6),new ItemStack(IEContent.blockMetalDevice,1,9)),
				new ManualPages.Crafting("", new ItemStack(IEContent.itemMaterial,1,2),new ItemStack(IEContent.blockWoodenDevice,1,2)),
				new ManualPages.Text("generatorWindmill"),
				new ManualPages.Crafting("", new ItemStack(IEContent.itemMaterial,1,1),new ItemStack(IEContent.blockWoodenDevice,1,1)),
				new ManualPages.Text("generatorWatermill"),
				new ManualPages.Crafting("", new ItemStack(IEContent.itemMaterial,1,4),new ItemStack(IEContent.itemMaterial,1,5)),
				new ManualPages.Crafting("generatorWindmillImproved", new ItemStack(IEContent.blockWoodenDevice,1,4))
		);
				

		GuiManual.addEntry("blastfurnace", new ManualPages.Text("blastfurnace0"), new ManualPages.Crafting("blastfurnaceBlock", new ItemStack(IEContent.blockStoneDevice,1,2)));

		GuiManual.addEntry("highvoltage", new ManualPages.Text("highvoltage0"),
				new ManualPages.Crafting("", new ItemStack(IEContent.blockMetalDevice,1,8),new ItemStack(IEContent.blockMetalDevice,1,4)),
				new ManualPages.Crafting("", new ItemStack(IEContent.blockMetalDevice,1,5),new ItemStack(IEContent.blockMetalDevice,1,7)));
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		if(ID==Lib.GUIID_CokeOven && world.getTileEntity(x, y, z) instanceof TileEntityCokeOven)
			return new GuiCokeOven(player.inventory, (TileEntityCokeOven) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_BlastFurnace && world.getTileEntity(x, y, z) instanceof TileEntityBlastFurnace)
			return new GuiBlastFurnace(player.inventory, (TileEntityBlastFurnace) world.getTileEntity(x, y, z));
		if(ID==Lib.GUIID_Revolver && player.getCurrentEquippedItem()!=null && player.getCurrentEquippedItem().getItem() instanceof ItemRevolver)
			return new GuiRevolver(player.inventory, world);
		if(ID==Lib.GUIID_Manual && player.getCurrentEquippedItem()!=null && OreDictionary.itemMatches(new ItemStack(IEContent.itemTool,1,3), player.getCurrentEquippedItem(), false))
			return new GuiManual(player);
		return null;
	}

}