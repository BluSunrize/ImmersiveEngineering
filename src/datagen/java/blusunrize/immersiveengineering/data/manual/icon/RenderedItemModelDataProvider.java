/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.manual.icon;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.wires.IEWireTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public record RenderedItemModelDataProvider(
        PackOutput output, ExistingFileHelper helper, Path itemOutputDirectory
) implements DataProvider
{
    @Override
    public CompletableFuture<?> run(CachedOutput pCache)
    {
        GameInitializationManager.getInstance().initialize(helper, output);
        IEWireTypes.setup();

        try(ModelRenderer itemRenderer = new ModelRenderer(256, 256, itemOutputDirectory.toFile()))
        {
            Set<String> domainsToRender = Set.of("minecraft", Lib.MODID);
            ForgeRegistries.ITEMS.getEntries().forEach(entry -> {
                ResourceLocation name = entry.getKey().location();
                if(!domainsToRender.contains(name.getNamespace()))
                    return;
                Item item = entry.getValue();
                ModelResourceLocation modelLocation = new ModelResourceLocation(name, "inventory");
                ItemStack stackToRender = item.getDefaultInstance();

                final BakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
                itemRenderer.renderModel(model, name.getNamespace()+"/"+name.getPath()+".png", stackToRender);
            });
        }
        return CompletableFuture.completedFuture(null);
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "Item Renderer";
    }
}
