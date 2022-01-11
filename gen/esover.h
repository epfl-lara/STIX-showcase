#ifndef __ESOVER_H__
#define __ESOVER_H__

/* --------------------------- preprocessor macros ----- */

#define STAINLESS_FUNC_PURE
#if defined(__cplusplus)
#undef STAINLESS_FUNC_PURE
#define STAINLESS_FUNC_PURE _Pragma("FUNC_IS_PURE;")
#elif __GNUC__>=3
#undef STAINLESS_FUNC_PURE
#define STAINLESS_FUNC_PURE __attribute__((__pure__))
#elif defined(__has_attribute)
#if __has_attribute(pure)
#undef STAINLESS_FUNC_PURE
#define STAINLESS_FUNC_PURE __attribute__((__pure__))
#endif
#endif

/* ------------------------------------- macros ----- */

#define E_FILE_BLOCK_STAT_ERROR 3
#define E_MEMFILESYSTEM_FILETYPE_LAST 9
#define E_MEMFILESYSTEM_FILETYPE_CONTEXT1 7
#define E_MEMFILESYSTEM_FILETYPE_LUT 8
#define E_FILE_BLOCK_STAT_FILE 2
#define E_FILE_BLOCK_STAT_BAD 4
#define E_FILE_BLOCK_STAT_FREE 1
#define E_MEMFILESYSTEM_FILETYPE_RAW_COUNTS 2
#define E_FILE_BLOCK_STAT_UNKNOWN 0
#define E_MEMFILESYSTEM_FILETYPE_CONTEXT0 6
#define E_MEMFILESYSTEM_FILETYPE_FLARE 4
#define E_MEMFILESYSTEM_FILETYPE_ASPECT 1
#define E_MEMFILESYSTEM_FILETYPE_TMMGMT 3
#define E_MEMFILESYSTEM_FILETYPE_USER_REQUESTS 5
#define E_MEMFILESYSTEM_FILETYPE_FIRST 0


/* ----------------------------------- includes ----- */

#include <assert.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>




/* ---------------------- data type definitions ----- */

typedef struct {
  uint32_t controlBlockNumber;
  uint8_t fileCatalog[60];
  uint32_t freeBlocksBitMap[2];
} FileControlBlock;

typedef struct {
  uint32_t coarse_46;
  uint16_t fine_46;
} TimingSCET_14;

typedef struct {
  int32_t cbIdx_125;
  uint32_t fileIndex_29;
  uint32_t coarse_42;
  uint16_t fine_42;
  uint16_t duration_21;
} QueueElement_14;

typedef struct {
  TimingSCET_14 _1;
  QueueElement_14 _2;
} Tuple_TimingSCET_14_QueueElement_14;

typedef struct {
  Tuple_TimingSCET_14_QueueElement_14* data;
  int32_t length;
} array_Tuple_TimingSCET_14_QueueElement_14;

typedef struct {
  array_Tuple_TimingSCET_14_QueueElement_14 array_174;
  bool (*order_150)(TimingSCET_14, TimingSCET_14);
} SortedArray_21_TimingSCET_14_QueueElement_14;

typedef struct {
  SortedArray_21_TimingSCET_14_QueueElement_14 sortedArray_21;
  TimingSCET_14 min_45;
  TimingSCET_14 max_85;
} PriorityQueue_14;



/* ---------------------- function declarations ----- */

STAINLESS_FUNC_PURE void initPrioQueue(int32_t l_367, TimingSCET_14 to_47, TimingSCET_14 from_31);
bool insertIntoPrioQueue(PriorityQueue_14* queue_21, TimingSCET_14 time_7, QueueElement_14 elem_78);
void setBlockAsFree(int32_t cbIdx_123, uint32_t blockNumber_71);

#endif
