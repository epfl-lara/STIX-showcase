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

#define E_MEMFILESYSTEM_FILETYPE_CONTEXT1 7
#define E_MEMFILESYSTEM_FILETYPE_CONTEXT0 6
#define E_MEMFILESYSTEM_FILETYPE_LAST 9
#define E_FILE_BLOCK_STAT_UNKNOWN 0
#define E_MEMFILESYSTEM_FILETYPE_FLARE 4
#define E_FILE_BLOCK_STAT_ERROR 3
#define E_MEMFILESYSTEM_FILETYPE_RAW_COUNTS 2
#define E_FILE_BLOCK_STAT_FREE 1
#define E_MEMFILESYSTEM_FILETYPE_FIRST 0
#define E_MEMFILESYSTEM_FILETYPE_LUT 8
#define E_FILE_BLOCK_STAT_FILE 2
#define E_FILE_BLOCK_STAT_BAD 4
#define E_MEMFILESYSTEM_FILETYPE_TMMGMT 3
#define E_MEMFILESYSTEM_FILETYPE_ASPECT 1
#define E_MEMFILESYSTEM_FILETYPE_USER_REQUESTS 5


/* ----------------------------------- includes ----- */

#include <assert.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <string.h>




/* ---------------------- data type definitions ----- */

typedef struct {
  uint32_t coarse_4;
  uint16_t fine_4;
} TimingSCET_0;

typedef struct {
  uint32_t controlBlockNumber;
  uint8_t fileCatalog[60];
  uint32_t freeBlocksBitMap[2];
} FileControlBlock;



/* ---------------------- function declarations ----- */

STAINLESS_FUNC_PURE void initPrioQueue(int32_t l_38, TimingSCET_0 to_5, TimingSCET_0 from_3);
void setBlockAsFree(int32_t cbIdx_11, uint32_t blockNumber_8);

#endif
