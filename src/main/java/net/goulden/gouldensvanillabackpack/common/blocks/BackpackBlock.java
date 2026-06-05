package net.goulden.gouldensvanillabackpack.common.blocks;

import com.mojang.serialization.MapCodec;
import net.goulden.gouldensvanillabackpack.common.particles.BackpackBreakParticleOptions;
import net.goulden.gouldensvanillabackpack.registry.BPBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BackpackBlock extends BaseEntityBlock implements EntityBlock, SimpleWaterloggedBlock {
    protected static final VoxelShape SHAPE_X;
    protected static final VoxelShape SHAPE_Z;
    protected static final VoxelShape FLOATING_SHAPE_X;
    protected static final VoxelShape FLOATING_SHAPE_Z;
    public static final DirectionProperty FACING;
    public static final BooleanProperty FLOATING;
    public static final BooleanProperty WATERLOGGED;

    public BackpackBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(FLOATING, false)
                .setValue(WATERLOGGED, false));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(FLOATING, level.getFluidState(pos.below()).isSource() && !level.getFluidState(pos).isSource())
                .setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
    }

    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state.setValue(FLOATING, level.getFluidState(currentPos.below()).isSource());
    }

    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, BPBlockEntities.BACKPACK.get(), BackpackBlockEntity::tick);
    }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else if (player.isSpectator()) {
            return InteractionResult.CONSUME;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof BackpackBlockEntity backpackBlockEntity) {

                player.openMenu(backpackBlockEntity);
                backpackBlockEntity.onOpen(player);

                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.PASS;
            }
        }
    }

    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        Containers.dropContentsOnDestroy(state, newState, level, pos);
        super.onRemove(state, level, pos, newState, isMoving);
    }

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(FLOATING)) {
            return state.getValue(FACING).getAxis() == Direction.Axis.X ? FLOATING_SHAPE_Z : FLOATING_SHAPE_X;
        } else {
            return state.getValue(FACING).getAxis() == Direction.Axis.X ? SHAPE_Z : SHAPE_X;
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BackpackBlockEntity(pos, state);
    }

    public MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(FLOATING);
        builder.add(WATERLOGGED);
    }

    static {
        FACING = HorizontalDirectionalBlock.FACING;
        FLOATING = BooleanProperty.create("floating");
        WATERLOGGED = BlockStateProperties.WATERLOGGED;
        SHAPE_X = Shapes.or(
                Block.box(4.0, 0.0, 5.0, 12.0, 7.5, 11.0),
                Block.box(3.5, 7.5, 4.5, 12.5, 11.5, 11.5));
        SHAPE_Z = Shapes.or(
                Block.box(5.0, 0.0, 4.0, 11.0, 7.5, 12.0),
                Block.box(4.5, 7.5, 3.5, 11.5, 11.5, 12.5));
        FLOATING_SHAPE_X = Shapes.or(
                Block.box(4.0, -2.5, 5.0, 12.0, 5.0, 11.0),
                Block.box(3.5, 5.0, 4.5, 12.5, 9, 11.5));
        FLOATING_SHAPE_Z = Shapes.or(
                Block.box(5.0, -2.5, 4.0, 11.0, 5.0, 12.0),
                Block.box(4.5, 5.0, 3.5, 11.5, 9, 12.5));
    }

    @Override
    public void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        level.playSound(null, pos, state.getSoundType().getBreakSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (level.getBlockEntity(pos) instanceof BackpackBlockEntity be) {
            int baseColor = be.getBaseColor();
            int lidColor = be.getLidColor();
            for (int i = 0; i < 10; i++) {
                double x = pos.getX() + level.random.nextDouble();
                double y = pos.getY() + level.random.nextDouble();
                double z = pos.getZ() + level.random.nextDouble();
                double vx = (level.random.nextDouble() - 0.5) * 0.1;
                double vy = level.random.nextDouble() * 0.1;
                double vz = (level.random.nextDouble() - 0.5) * 0.1;
                if (baseColor != 0)
                    level.addParticle(new BackpackBreakParticleOptions(baseColor), x, y, z, vx, vy, vz);
                if (lidColor != 0)
                    level.addParticle(new BackpackBreakParticleOptions(lidColor), x, y, z, vx, vy, vz);
            }
        } else {
            super.spawnDestroyParticles(level, player, pos, state);
        }
    }
}
