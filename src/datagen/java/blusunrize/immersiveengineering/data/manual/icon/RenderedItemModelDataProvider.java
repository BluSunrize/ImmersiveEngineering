/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.manual.icon;

import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.wires.IEWireTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;

public record RenderedItemModelDataProvider(
        DataGenerator generator, ExistingFileHelper helper, Path itemOutputDirectory
) implements DataProvider
{
    @Override
    public void run(CachedOutput pCache) throws IOException
    {
        GameInitializationManager.getInstance().initialize(helper, generator);
        IEWireTypes.setup();

        Field item_renderProperties;
        try
        {
            item_renderProperties = Item.class.getDeclaredField("renderProperties");
            item_renderProperties.setAccessible(true);
        } catch(NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
        try(ModelRenderer itemRenderer = new ModelRenderer(256, 256, itemOutputDirectory.toFile()))
        {
            IEItems.REGISTER.getEntries().forEach(regEntry -> {
                Item item = regEntry.get();
                item.initializeClient(properties -> {
                    try
                    {
                        item_renderProperties.set(item, properties);
                    } catch(IllegalAccessException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
                ResourceLocation name = regEntry.getId();
                ModelResourceLocation modelLocation = new ModelResourceLocation(name, "inventory");

                final BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
                itemRenderer.renderModel(model, name.getNamespace()+"/"+name.getPath()+".png", new ItemStack(item));
            });
        }
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "Item Renderer";
    }
}
