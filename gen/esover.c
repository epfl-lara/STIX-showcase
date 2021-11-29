/* --------------------------- GenC requirements ----- */

#include <limits.h>
#if (__STDC_VERSION__ < 199901L) || (CHAR_BIT != 8)
#error "Your compiler does not meet the minimum requirements of GenC. Please see"
#error "https://epfl-lara.github.io/stainless/genc.html#requirements for more details."
#endif

/* ---------------------------- include header ------- */

#include "esover.h"

/* ----------------------------------- includes ----- */

#include <assert.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>




/* ---------------------- data type definitions ----- */

typedef struct {
  int32_t cbIdx_13;
  uint32_t fileIndex_1;
  uint32_t coarse_0;
  uint16_t fine_0;
  uint16_t duration_0;
} QueueElement_0;

typedef struct {
  TimingSCET_0 _1;
  QueueElement_0 _2;
} Tuple_TimingSCET_0_QueueElement_0;

typedef struct {
  Tuple_TimingSCET_0_QueueElement_0* data;
  int32_t length;
} array_Tuple_TimingSCET_0_QueueElement_0;

typedef struct {
  array_Tuple_TimingSCET_0_QueueElement_0 array_20;
  bool (*order_17)(TimingSCET_0, TimingSCET_0);
} SortedArray_0_TimingSCET_0_QueueElement_0;

typedef struct {
  SortedArray_0_TimingSCET_0_QueueElement_0 sortedArray_0;
  TimingSCET_0 min_3;
  TimingSCET_0 max_8;
} PriorityQueue_0;

typedef struct {
  uint32_t* data;
  int32_t length;
} array_uint32;


/* --------------------------- global variables ----- */

bool (*TimingSCETOrder_1)(TimingSCET_0, TimingSCET_0) = leq_2;
FileControlBlock memFilesystem[1090];


/* ---------------------- function declarations ----- */

STAINLESS_FUNC_PURE void ghost_0_unit(void);
STAINLESS_FUNC_PURE bool leq_2(TimingSCET_0 t1_1, TimingSCET_0 t2_1);
void updateStatsBlockTransition_0(uint8_t from_2, uint8_t to_2);


/* ----------------------- function definitions ----- */

STAINLESS_FUNC_PURE void ghost_0_unit(void) {
    
}

STAINLESS_FUNC_PURE void initPrioQueue(int32_t l_38, TimingSCET_0 to_5, TimingSCET_0 from_3) {
    int32_t stainless_length_0 = l_38;
    Tuple_TimingSCET_0_QueueElement_0 stainless_buffer_0[stainless_length_0];
    int32_t stainless_i_0;
    for (stainless_i_0 = 0; stainless_i_0 < stainless_length_0; ++stainless_i_0) {
        stainless_buffer_0[stainless_i_0] = (Tuple_TimingSCET_0_QueueElement_0) { ._1 = to_5, ._2 = (QueueElement_0) { .cbIdx_13 = 0, .fileIndex_1 = 0, .coarse_0 = to_5.coarse_4, .fine_0 = to_5.fine_4, .duration_0 = 0 } };
    };
    array_Tuple_TimingSCET_0_QueueElement_0 norm_0 = (array_Tuple_TimingSCET_0_QueueElement_0) { .data = stainless_buffer_0, .length = stainless_length_0 };
    array_Tuple_TimingSCET_0_QueueElement_0 norm_1 = norm_0;
    bool (*norm_2)(TimingSCET_0, TimingSCET_0) = TimingSCETOrder_1;
    SortedArray_0_TimingSCET_0_QueueElement_0 norm_3 = (SortedArray_0_TimingSCET_0_QueueElement_0) { .array_20 = norm_1, .order_17 = norm_2 };
    TimingSCET_0 norm_4 = from_3;
    TimingSCET_0 norm_5 = to_5;
    (PriorityQueue_0) { .sortedArray_0 = norm_3, .min_3 = norm_4, .max_8 = norm_5 };
}

STAINLESS_FUNC_PURE bool leq_2(TimingSCET_0 t1_1, TimingSCET_0 t2_1) {
    return (t1_1.coarse_4 < t2_1.coarse_4) || ((t1_1.coarse_4 == t2_1.coarse_4) && (t1_1.fine_4 <= t2_1.fine_4));
}

void setBlockAsFree(int32_t cbIdx_11, uint32_t blockNumber_8) {
    int32_t k_8 = ((int32_t)blockNumber_8) / ((int32_t)32);
    uint32_t v_22 = 1 << (blockNumber_8 % 32);
    array_uint32 ev$2_1 = (array_uint32) { .data = memFilesystem[cbIdx_11].freeBlocksBitMap, .length = 2 };
    ev$2_1.data[k_8] = ev$2_1.data[k_8] | v_22;
    uint8_t oldStatus_3 = memFilesystem[cbIdx_11].fileCatalog[(int32_t)blockNumber_8];
    updateStatsBlockTransition_0(oldStatus_3, E_FILE_BLOCK_STAT_FREE);
    memFilesystem[cbIdx_11].fileCatalog[(int32_t)blockNumber_8] = E_FILE_BLOCK_STAT_FREE;
    ghost_0_unit();
}

void updateStatsBlockTransition_0(uint8_t from_2, uint8_t to_2) {
    if (from_2 == E_FILE_BLOCK_STAT_FILE) {
        usedBlockCount = usedBlockCount - 1;
    } else if (from_2 == E_FILE_BLOCK_STAT_ERROR) {
        errorBlockCount = errorBlockCount - 1;
    } else if (from_2 == E_FILE_BLOCK_STAT_BAD) {
        badBlockCount = badBlockCount - 1;
    }
    if (to_2 == E_FILE_BLOCK_STAT_FILE) {
        usedBlockCount = usedBlockCount + 1;
    } else if (to_2 == E_FILE_BLOCK_STAT_ERROR) {
        errorBlockCount = errorBlockCount + 1;
    } else if (to_2 == E_FILE_BLOCK_STAT_BAD) {
        badBlockCount = badBlockCount + 1;
    }
}
