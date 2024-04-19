package tallestred.piglinproliferation.common.blocks;

import net.minecraft.core.Holder;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import tallestred.piglinproliferation.util.LazyLoadedArray;

//TODO see if this can use lazy loaded holder
public record BlockItemHolder<B extends Block, I extends BlockItem>(Holder<B> block, Holder<I> item) {
    private static LazyLoadedArray<BlockItemHolder<? extends Block, ? extends BlockItem>> ENTRIES = new LazyLoadedArray<>(14);

    public BlockItemHolder(Holder<B> block, Holder<I> item) {
        this.block = block;
        this.item = item;
     }

    public BlockItemHolder(DeferredHolder<Block, ? extends Block> block, DeferredHolder<Item, ? extends BlockItem> item) {
        var bHolder = (Holder<B>) block;
        var iHolder = (Holder<I>) item;
        this(, );
    }

    public static Block[] blocks() {
        var blocks = new Block[ENTRIES.lengtj]
    }

    public static Block[] items() {

    }
}
