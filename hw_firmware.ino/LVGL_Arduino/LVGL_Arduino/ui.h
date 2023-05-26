// SquareLine LVGL GENERATED FILE
// EDITOR VERSION: SquareLine Studio 1.1.1
// LVGL VERSION: 8.3.3
// PROJECT: SquareLine_Project

#ifndef _SQUARELINE_PROJECT_UI_H
#define _SQUARELINE_PROJECT_UI_H

#ifdef __cplusplus
extern "C" {
#endif

#if defined __has_include
#if __has_include("lvgl.h")
#include "lvgl.h"
#elif __has_include("lvgl/lvgl.h")
#include "lvgl/lvgl.h"
#else
#include "lvgl.h"
#endif
#else
#include "lvgl.h"
#endif

extern lv_obj_t * ui_Screen1;
extern lv_obj_t * ui_Screen1_Image1;
extern lv_obj_t * ui_Screen2;
extern lv_obj_t * ui_NextTurn;
extern lv_obj_t * ui_Battery;
extern lv_obj_t * ui_TimeDistance;
extern lv_obj_t * ui_Screen2_Label4;
extern lv_obj_t * ui_Screen2_Image2;
extern lv_obj_t * ui_Destination;
extern lv_obj_t * ui_NextM;
extern lv_obj_t * ui_Screen2_Image3;
extern lv_obj_t * ui_Screen2_Image4;
extern lv_obj_t * ui_Screen2_Image5;


LV_IMG_DECLARE(ui_img_logo_png);    // assets\logo.png
LV_IMG_DECLARE(ui_img_vector_png);    // assets\Vector.png
LV_IMG_DECLARE(ui_img_whatsapp_png);    // assets\whatsapp.png
LV_IMG_DECLARE(ui_img_zap_png);    // assets\zap.png
LV_IMG_DECLARE(ui_arrow_upward);    // assets\zap.png

LV_FONT_DECLARE(ui_font_InterExtraBold10);
LV_FONT_DECLARE(ui_font_InterExtraBold18);
LV_FONT_DECLARE(ui_font_InterMedium10);


void ui_init(void);

#ifdef __cplusplus
} /*extern "C"*/
#endif

#endif
