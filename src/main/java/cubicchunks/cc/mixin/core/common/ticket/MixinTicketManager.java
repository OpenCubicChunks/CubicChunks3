package cubicchunks.cc.mixin.core.common.ticket;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import cubicchunks.cc.chunk.IChunkManager;
import cubicchunks.cc.chunk.ICubeHolder;
import cubicchunks.cc.chunk.cube.Cube;
import cubicchunks.cc.chunk.graph.CCTicketType;
import cubicchunks.cc.chunk.ticket.*;
import cubicchunks.cc.chunk.util.CubePos;
import cubicchunks.cc.mixin.core.common.chunk.interfaces.InvokeChunkHolder;
import cubicchunks.cc.mixin.core.common.chunk.interfaces.InvokeChunkManager;
import cubicchunks.cc.mixin.core.common.ticket.interfaces.InvokeTicket;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.concurrent.ITaskExecutor;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.server.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TicketManager.class)
public abstract class MixinTicketManager implements ITicketManager {
    private final Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> cubeTickets = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<ObjectSet<ServerPlayerEntity>> playersBySectionPos = new Long2ObjectOpenHashMap<>();

    private final Set<ChunkHolder> cubeHolders = new HashSet<>();
    private final LongSet sectionPositions = new LongOpenHashSet();


    //@Final @Shadow private Long2ObjectMap<ObjectSet<ServerPlayerEntity>> playersByChunkPos;
    //@Final @Shadow private LongSet chunkPositions;
    @Final @Shadow private Executor field_219388_p;
    @Shadow private long currentTime;
    //@Final @Shadow private Set<ChunkHolder> chunkHolders;

    @Shadow protected static int getLevel(SortedArraySet<Ticket<?>> p_229844_0_) {
        throw new Error("Mixin did not apply correctly");
    }

    @Shadow protected abstract void register(long chunkPosIn, Ticket<?> ticketIn);

    private final CubeTicketTracker cubeTicketTracker = new CubeTicketTracker(this);
    private final PlayerSectionTracker playerSectionTracker = new PlayerSectionTracker(this, 8);
    private final PlayerSectionTicketTracker playerSectionTicketTracker = new PlayerSectionTicketTracker(this, 33);
    private CubeTaskPriorityQueueSorter cubeTaskPriorityQueueSorter;
    private ITaskExecutor<CubeTaskPriorityQueueSorter.FunctionEntry<Runnable>> playerSectionTicketThrottler;
    private ITaskExecutor<CubeTaskPriorityQueueSorter.RunnableEntry> playerSectionTicketThrottlerSorter;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(Executor executor, Executor executor2, CallbackInfo ci) {
        ITaskExecutor<Runnable> itaskexecutor = ITaskExecutor.inline("player ticket throttler", executor2::execute);
        CubeTaskPriorityQueueSorter
                cubeTaskPriorityQueueSorter = new CubeTaskPriorityQueueSorter(ImmutableList.of(itaskexecutor), executor, 4);
        this.cubeTaskPriorityQueueSorter = cubeTaskPriorityQueueSorter;
        this.playerSectionTicketThrottler = cubeTaskPriorityQueueSorter.createExecutor(itaskexecutor, true);
        this.playerSectionTicketThrottlerSorter = cubeTaskPriorityQueueSorter.createSorterExecutor(itaskexecutor);
    }

    @Override
    public void registerSection(long cubePosIn, Ticket<?> ticketIn) {
        SortedArraySet<Ticket<?>> sortedarrayset = this.getSectionTicketSet(cubePosIn);
        int i = getLevel(sortedarrayset);
        Ticket<?> ticket = sortedarrayset.func_226175_a_(ticketIn);
        ((InvokeTicket) ticket).setTimestampCC(this.currentTime);
        if (ticketIn.getLevel() < i) {
            this.cubeTicketTracker.updateSourceLevel(cubePosIn, ticketIn.getLevel(), true);
        }
        this.register(CubePos.from(cubePosIn).asChunkPos().asLong(), ticketIn);
    }

    @Override
    public void releaseCube(long cubePosIn, Ticket<?> ticketIn) {
        SortedArraySet<Ticket<?>> sortedarrayset = this.getSectionTicketSet(cubePosIn);
        sortedarrayset.remove(ticketIn);

        if (sortedarrayset.isEmpty()) {
            this.cubeTickets.remove(cubePosIn);
        }

        this.cubeTicketTracker.updateSourceLevel(cubePosIn, getLevel(sortedarrayset), false);
    }

    private SortedArraySet<Ticket<?>> getSectionTicketSet(long cubePos) {
        return this.cubeTickets.computeIfAbsent(cubePos, (p_229851_0_) -> SortedArraySet.newSet(4));
    }

    @Inject(method = "setViewDistance", at = @At("HEAD"))
    protected void setViewDistance(int viewDistance, CallbackInfo ci)
    {
        this.playerSectionTicketTracker.setViewDistance(viewDistance);
    }

    //BEGIN INJECT

    @Inject(method = "processUpdates", at = @At("RETURN"), cancellable = true)
    public void processUpdates(ChunkManager chunkManager, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        this.playerSectionTracker.processAllUpdates();
        this.playerSectionTicketTracker.processAllUpdates();
        int i = Integer.MAX_VALUE - this.cubeTicketTracker.update(Integer.MAX_VALUE);
        boolean flag = i != 0;
        if (!this.cubeHolders.isEmpty()) {
            this.cubeHolders.forEach((cubeHolder) -> ((InvokeChunkHolder) cubeHolder).processUpdatesCC(chunkManager));
            this.cubeHolders.clear();
            callbackInfoReturnable.setReturnValue(true);
            return;
        } else {
            if (!this.sectionPositions.isEmpty()) {
                LongIterator longiterator = this.sectionPositions.iterator();

                while (longiterator.hasNext()) {
                    long j = longiterator.nextLong();
                    if (this.getSectionTicketSet(j).stream().anyMatch((p_219369_0_) -> p_219369_0_.getType() == CCTicketType.CCPLAYER)) {
                        ChunkHolder chunkholder = ((IChunkManager) chunkManager).getCubeHolder(j);
                        if (chunkholder == null) {
                            throw new IllegalStateException();
                        }

                        CompletableFuture<Either<Cube, ChunkHolder.IChunkLoadingError>> sectionEntityTickingFuture =
                                ((ICubeHolder)chunkholder).getSectionEntityTickingFuture();
                        sectionEntityTickingFuture.thenAccept((p_219363_3_) -> this.field_219388_p.execute(() -> {
                            this.playerSectionTicketThrottlerSorter.enqueue(CubeTaskPriorityQueueSorter.createSorterMsg(() -> {
                            }, j, false));
                        }));
                    }
                }
                this.sectionPositions.clear();
            }
            callbackInfoReturnable.setReturnValue(flag | callbackInfoReturnable.getReturnValueZ());
            return;
        }
    }

    //BEGIN OVERWRITE

    //TODO: check if there is another way to do this
    /**
     * @author NotStirred
     * @reason idk & cba
     */
    @Inject(method = "tick", at = @At("RETURN"))
    protected void tickSection(CallbackInfo ci) {
        ObjectIterator<Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>>> objectiterator = this.cubeTickets.long2ObjectEntrySet().fastIterator();
        while (objectiterator.hasNext()) {
            Long2ObjectMap.Entry<SortedArraySet<Ticket<?>>> entry = objectiterator.next();
            if (entry.getValue().removeIf((ticket) -> ((InvokeTicket) ticket).cc$isexpired(this.currentTime))) {
                this.cubeTicketTracker.updateSourceLevel(entry.getLongKey(), getLevel(entry.getValue()), false);
            }
            if (entry.getValue().isEmpty()) {
                objectiterator.remove();
            }
        }
    }

    //BEGIN OVERRIDES
    @Override
    public <T> void registerWithLevel(TicketType<T> type, CubePos pos, int level, T value) {
        this.registerSection(pos.asLong(), new Ticket<>(type, level, value));
    }

    @Override
    public <T> void releaseWithLevel(TicketType<T> type, CubePos pos, int level, T value) {
        Ticket<T> ticket = new Ticket<>(type, level, value);
        this.releaseCube(pos.asLong(), ticket);
    }

    @Override
    public <T> void register(TicketType<T> type, CubePos pos, int distance, T value) {
        this.registerSection(pos.asLong(), new Ticket<>(type, 33 - distance, value));
    }

    @Override
    public <T> void release(TicketType<T> type, CubePos pos, int distance, T value) {
        Ticket<T> ticket = new Ticket<>(type, 33 - distance, value);
        this.releaseCube(pos.asLong(), ticket);
    }

    @Override
    public void updatePlayerPosition(CubePos cubePosIn, ServerPlayerEntity player) {
        long i = cubePosIn.asLong();
        this.playersBySectionPos.computeIfAbsent(i, (x) -> new ObjectOpenHashSet<>()).add(player);
        this.playerSectionTracker.updateSourceLevel(i, 0, true);
        this.playerSectionTicketTracker.updateSourceLevel(i, 0, true);
    }

    @Override
    public void removePlayer(CubePos cubePosIn, ServerPlayerEntity player) {
        long i = cubePosIn.asLong();
        ObjectSet<ServerPlayerEntity> objectset = this.playersBySectionPos.get(i);
        objectset.remove(player);
        if (objectset.isEmpty()) {
            this.playersBySectionPos.remove(i);
            this.playerSectionTracker.updateSourceLevel(i, Integer.MAX_VALUE, false);
            this.playerSectionTicketTracker.updateSourceLevel(i, Integer.MAX_VALUE, false);
        }
    }

    /**
     * Returns the number of chunks taken into account when calculating the mob cap
     */
    @Override
    public int getSpawningSectionsCount() {
        this.playerSectionTracker.processAllUpdates();
        return this.playerSectionTracker.sectionsInRange.size();
    }

    @Override
    public boolean isSectionOutsideSpawningRadius(long cubePosIn) {
        this.playerSectionTracker.processAllUpdates();
        return this.playerSectionTracker.sectionsInRange.containsKey(cubePosIn);
    }

    @Override
    public Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> getCubeTickets() {
        return cubeTickets;
    }

    @Override
    public ITaskExecutor<CubeTaskPriorityQueueSorter.FunctionEntry<Runnable>> getSectionPlayerTicketThrottler() {
        return playerSectionTicketThrottler;
    }

    @Override
    public ITaskExecutor<CubeTaskPriorityQueueSorter.RunnableEntry> getPlayerSectionTicketThrottlerSorter() { return playerSectionTicketThrottlerSorter; }

    @Override
    public LongSet getSectionPositions() {
        return sectionPositions;
    }

    @Override
    public Set<ChunkHolder> getCubeHolders()
    {
        return this.cubeHolders;
    }

    @Override
    public Executor executor() {
        return field_219388_p;
    }

    @Override
    public CubeTaskPriorityQueueSorter getCubeTaskPriorityQueueSorter() {
        return cubeTaskPriorityQueueSorter;
    }

    @Override
    public Long2ObjectMap<ObjectSet<ServerPlayerEntity>> getPlayersBySectionPos()
    {
        return this.playersBySectionPos;
    }
}