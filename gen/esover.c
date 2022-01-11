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
  uint32_t* data;
  int32_t length;
} array_uint32;


/* --------------------------- global variables ----- */

FileControlBlock memFilesystem[1090];
bool (*TimingSCETOrder_15)(TimingSCET_14, TimingSCET_14) = leq_37;


/* ---------------------- function declarations ----- */

STAINLESS_FUNC_PURE int32_t findIndex_7_TimingSCET_14_QueueElement_14(array_Tuple_TimingSCET_14_QueueElement_14 array_173, bool (*order_149)(TimingSCET_14, TimingSCET_14), TimingSCET_14 key_87);
STAINLESS_FUNC_PURE void ghost_7_unit(void);
bool insert_14(PriorityQueue_14* thiss_427, array_Tuple_TimingSCET_14_QueueElement_14 elemRef_14);
bool insert_15_TimingSCET_14_QueueElement_14(SortedArray_21_TimingSCET_14_QueueElement_14* thiss_389, array_Tuple_TimingSCET_14_QueueElement_14 elemRef_15);
STAINLESS_FUNC_PURE bool leq_35(uint32_t c1_7, uint16_t f1_7, uint32_t c2_7, uint16_t f2_15);
STAINLESS_FUNC_PURE bool leq_37(TimingSCET_14 t1_22, TimingSCET_14 t2_22);
void updateStatsBlockTransition_7(uint8_t from_30, uint8_t to_44);


/* ----------------------- function definitions ----- */

STAINLESS_FUNC_PURE int32_t findIndex_7_TimingSCET_14_QueueElement_14(array_Tuple_TimingSCET_14_QueueElement_14 array_173, bool (*order_149)(TimingSCET_14, TimingSCET_14), TimingSCET_14 key_87) {
    int32_t n_150 = array_173.length;
    int32_t l_375 = -1;
    int32_t h_438 = array_173.length;
    while ((l_375 + 1) != h_438) {
        int32_t m_103 = (int32_t)(((uint32_t)(l_375 + h_438)) >> 1);
        if (order_149(key_87, array_173.data[m_103]._1)) {
            h_438 = m_103;
            ghost_7_unit();
        } else {
            l_375 = m_103;
        }
    }
    return h_438;
}

STAINLESS_FUNC_PURE void ghost_7_unit(void) {
    
}

STAINLESS_FUNC_PURE void initPrioQueue(int32_t l_367, TimingSCET_14 to_47, TimingSCET_14 from_31) {
    int32_t stainless_length_7 = l_367;
    Tuple_TimingSCET_14_QueueElement_14 stainless_buffer_15[stainless_length_7];
    int32_t stainless_i_7;
    for (stainless_i_7 = 0; stainless_i_7 < stainless_length_7; ++stainless_i_7) {
        stainless_buffer_15[stainless_i_7] = (Tuple_TimingSCET_14_QueueElement_14) { ._1 = to_47, ._2 = (QueueElement_14) { .cbIdx_125 = 0, .fileIndex_29 = 0, .coarse_42 = to_47.coarse_46, .fine_42 = to_47.fine_46, .duration_21 = 0 } };
    };
    array_Tuple_TimingSCET_14_QueueElement_14 norm_3 = (array_Tuple_TimingSCET_14_QueueElement_14) { .data = stainless_buffer_15, .length = stainless_length_7 };
    array_Tuple_TimingSCET_14_QueueElement_14 norm_4 = norm_3;
    bool (*norm_5)(TimingSCET_14, TimingSCET_14) = TimingSCETOrder_15;
    SortedArray_21_TimingSCET_14_QueueElement_14 norm_6 = (SortedArray_21_TimingSCET_14_QueueElement_14) { .array_174 = norm_4, .order_150 = norm_5 };
    TimingSCET_14 norm_7 = from_31;
    TimingSCET_14 norm_8 = to_47;
    (PriorityQueue_14) { .sortedArray_21 = norm_6, .min_45 = norm_7, .max_85 = norm_8 };
}

bool insertIntoPrioQueue(PriorityQueue_14* queue_21, TimingSCET_14 time_7, QueueElement_14 elem_78) {
    uint32_t c_71 = time_7.coarse_46;
    uint16_t f_303 = time_7.fine_46;
    uint32_t d_7 = (uint32_t)elem_78.duration_21;
    uint32_t minC_7 = queue_21->min_45.coarse_46;
    uint16_t minF_7 = queue_21->min_45.fine_46;
    uint32_t maxC_7 = queue_21->max_85.coarse_46;
    uint16_t maxF_7 = queue_21->max_85.fine_46;
    bool caseA_7 = leq_35(minC_7, minF_7, c_71, f_303) && leq_35(c_71, f_303, maxC_7, maxF_7);
    bool caseB_7 = leq_35(minC_7, minF_7, c_71 + d_7, f_303) && leq_35(c_71 + d_7, f_303, maxC_7, maxF_7);
    bool caseC_7 = leq_35(c_71, f_303, minC_7, minF_7) && leq_35(maxC_7, maxF_7, c_71 + d_7, f_303);
    if ((caseA_7 || caseB_7) || caseC_7) {
        PriorityQueue_14* norm_1 = queue_21;
        Tuple_TimingSCET_14_QueueElement_14 stainless_buffer_14[1] = { (Tuple_TimingSCET_14_QueueElement_14) { ._1 = time_7, ._2 = elem_78 } };
        array_Tuple_TimingSCET_14_QueueElement_14 norm_0 = (array_Tuple_TimingSCET_14_QueueElement_14) { .data = stainless_buffer_14, .length = 1 };
        array_Tuple_TimingSCET_14_QueueElement_14 norm_2 = norm_0;
        return insert_14(norm_1, norm_2);
    } else {
        return false;
    }
}

bool insert_14(PriorityQueue_14* thiss_427, array_Tuple_TimingSCET_14_QueueElement_14 elemRef_14) {
    return insert_15_TimingSCET_14_QueueElement_14(&thiss_427->sortedArray_21, elemRef_14);
}

bool insert_15_TimingSCET_14_QueueElement_14(SortedArray_21_TimingSCET_14_QueueElement_14* thiss_389, array_Tuple_TimingSCET_14_QueueElement_14 elemRef_15) {
    Tuple_TimingSCET_14_QueueElement_14 elem_87 = elemRef_15.data[0];
    int32_t n_151 = thiss_389->array_174.length;
    if (thiss_389->order_150(thiss_389->array_174.data[n_151 - 1]._1, elem_87._1)) {
        return false;
    }
    int32_t h_439 = findIndex_7_TimingSCET_14_QueueElement_14(thiss_389->array_174, thiss_389->order_150, elem_87._1);
    int32_t i_303 = n_151 - 1;
    while (i_303 > h_439) {
        Tuple_TimingSCET_14_QueueElement_14 tmp_14 = thiss_389->array_174.data[i_303];
        thiss_389->array_174.data[i_303] = thiss_389->array_174.data[i_303 - 1];
        thiss_389->array_174.data[i_303 - 1] = tmp_14;
        i_303 = i_303 - 1;
    }
    Tuple_TimingSCET_14_QueueElement_14 tmp_15 = thiss_389->array_174.data[h_439];
    thiss_389->array_174.data[h_439] = elemRef_15.data[0];
    elemRef_15.data[0] = tmp_15;
    return true;
}

STAINLESS_FUNC_PURE bool leq_35(uint32_t c1_7, uint16_t f1_7, uint32_t c2_7, uint16_t f2_15) {
    return (c1_7 < c2_7) || ((c1_7 == c2_7) && (f1_7 <= f2_15));
}

STAINLESS_FUNC_PURE bool leq_37(TimingSCET_14 t1_22, TimingSCET_14 t2_22) {
    return (t1_22.coarse_46 < t2_22.coarse_46) || ((t1_22.coarse_46 == t2_22.coarse_46) && (t1_22.fine_46 <= t2_22.fine_46));
}

void setBlockAsFree(int32_t cbIdx_123, uint32_t blockNumber_71) {
    int32_t k_78 = ((int32_t)blockNumber_71) / ((int32_t)32);
    uint32_t v_183 = 1 << (blockNumber_71 % 32);
    array_uint32 ev$2_15 = (array_uint32) { .data = memFilesystem[cbIdx_123].freeBlocksBitMap, .length = 2 };
    ev$2_15.data[k_78] = ev$2_15.data[k_78] | v_183;
    uint8_t oldStatus_31 = memFilesystem[cbIdx_123].fileCatalog[(int32_t)blockNumber_71];
    updateStatsBlockTransition_7(oldStatus_31, E_FILE_BLOCK_STAT_FREE);
    memFilesystem[cbIdx_123].fileCatalog[(int32_t)blockNumber_71] = E_FILE_BLOCK_STAT_FREE;
    ghost_7_unit();
}

void updateStatsBlockTransition_7(uint8_t from_30, uint8_t to_44) {
    if (from_30 == E_FILE_BLOCK_STAT_FILE) {
        usedBlockCount = usedBlockCount - 1;
    } else if (from_30 == E_FILE_BLOCK_STAT_ERROR) {
        errorBlockCount = errorBlockCount - 1;
    } else if (from_30 == E_FILE_BLOCK_STAT_BAD) {
        badBlockCount = badBlockCount - 1;
    }
    if (to_44 == E_FILE_BLOCK_STAT_FILE) {
        usedBlockCount = usedBlockCount + 1;
    } else if (to_44 == E_FILE_BLOCK_STAT_ERROR) {
        errorBlockCount = errorBlockCount + 1;
    } else if (to_44 == E_FILE_BLOCK_STAT_BAD) {
        badBlockCount = badBlockCount + 1;
    }
}
