// SquareLine LVGL GENERATED FILE
// EDITOR VERSION: SquareLine Studio 1.1.1
// LVGL VERSION: 8.3.3
// PROJECT: SquareLine_Project

#include "ui.h"
#include "ui_helpers.h"

///////////////////// VARIABLES ////////////////////
lv_obj_t * ui_Screen1;
lv_obj_t * ui_Screen1_Image1;
lv_obj_t * ui_Screen2;
lv_obj_t * ui_NextTurn;
lv_obj_t * ui_Battery;
lv_obj_t * ui_TimeDistance;
lv_obj_t * ui_Screen2_Label4;
lv_obj_t * ui_Screen2_Image2;
lv_obj_t * ui_Destination;
lv_obj_t * ui_NextM;
lv_obj_t * ui_Screen2_Image3;
lv_obj_t * ui_Screen2_Image4;

///////////////////// TEST LVGL SETTINGS ////////////////////
#if LV_COLOR_DEPTH != 16
    #error "LV_COLOR_DEPTH should be 16bit to match SquareLine Studio's settings"
#endif
#if LV_COLOR_16_SWAP !=0
    #error "LV_COLOR_16_SWAP should be 0 to match SquareLine Studio's settings"
#endif

///////////////////// ANIMATIONS ////////////////////

///////////////////// FUNCTIONS ////////////////////

///////////////////// SCREENS ////////////////////
void ui_Screen1_screen_init(void)
{
    ui_Screen1 = lv_obj_create(NULL);
    lv_obj_clear_flag(ui_Screen1, LV_OBJ_FLAG_SCROLLABLE);      /// Flags
    lv_obj_set_style_bg_color(ui_Screen1, lv_color_hex(0x000000), LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_bg_opa(ui_Screen1, 2550, LV_PART_MAIN | LV_STATE_DEFAULT);

    ui_Screen1_Image1 = lv_img_create(ui_Screen1);
    lv_img_set_src(ui_Screen1_Image1, &ui_img_logo_png);
    lv_obj_set_width(ui_Screen1_Image1, LV_SIZE_CONTENT);   /// 1
    lv_obj_set_height(ui_Screen1_Image1, LV_SIZE_CONTENT);    /// 1
    lv_obj_set_align(ui_Screen1_Image1, LV_ALIGN_CENTER);
    lv_obj_add_flag(ui_Screen1_Image1, LV_OBJ_FLAG_ADV_HITTEST);     /// Flags
    lv_obj_clear_flag(ui_Screen1_Image1, LV_OBJ_FLAG_SCROLLABLE);      /// Flags

}
void ui_Screen2_screen_init(void)
{
    ui_Screen2 = lv_obj_create(NULL);
    lv_obj_clear_flag(ui_Screen2, LV_OBJ_FLAG_SCROLLABLE);      /// Flags

    ui_NextTurn = lv_label_create(ui_Screen2);
    lv_obj_set_width(ui_NextTurn, 94);
    lv_obj_set_height(ui_NextTurn, 34);
    lv_obj_set_x(ui_NextTurn, 69);
    lv_obj_set_y(ui_NextTurn, 49);
    lv_label_set_text(ui_NextTurn, "Jl. Next Turn street Lorem Ipsum Dolor Sit Amet");
    lv_obj_set_style_text_color(ui_NextTurn, lv_color_hex(0x00FF00), LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_opa(ui_NextTurn, 255, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_align(ui_NextTurn, LV_TEXT_ALIGN_AUTO, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_font(ui_NextTurn, &ui_font_InterExtraBold10, LV_PART_MAIN | LV_STATE_DEFAULT);

    ui_Battery = lv_label_create(ui_Screen2);
    lv_obj_set_width(ui_Battery, LV_SIZE_CONTENT);   /// 1
    lv_obj_set_height(ui_Battery, LV_SIZE_CONTENT);    /// 1
    lv_obj_set_x(ui_Battery, -6);
    lv_obj_set_y(ui_Battery, 6);
    lv_obj_set_align(ui_Battery, LV_ALIGN_TOP_RIGHT);
    lv_label_set_text(ui_Battery, "100%");
    lv_obj_set_style_text_color(ui_Battery, lv_color_hex(0x00FF00), LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_opa(ui_Battery, 255, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_align(ui_Battery, LV_TEXT_ALIGN_RIGHT, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_font(ui_Battery, &ui_font_InterMedium10, LV_PART_MAIN | LV_STATE_DEFAULT);

    ui_TimeDistance = lv_label_create(ui_Screen2);
    lv_obj_set_width(ui_TimeDistance, 200);
    lv_obj_set_height(ui_TimeDistance, 14);
    lv_obj_set_x(ui_TimeDistance, 6);
    lv_obj_set_y(ui_TimeDistance, -20);
    lv_obj_set_align(ui_TimeDistance, LV_ALIGN_BOTTOM_LEFT);
    lv_label_set_text(ui_TimeDistance, "10 min - 1km");
    lv_obj_set_style_text_color(ui_TimeDistance, lv_color_hex(0x00FF00), LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_opa(ui_TimeDistance, 255, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_align(ui_TimeDistance, LV_TEXT_ALIGN_LEFT, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_font(ui_TimeDistance, &ui_font_InterMedium10, LV_PART_MAIN | LV_STATE_DEFAULT);

    ui_Screen2_Label4 = lv_label_create(ui_Screen2);
    lv_obj_set_width(ui_Screen2_Label4, 28);
    lv_obj_set_height(ui_Screen2_Label4, 14);
    lv_obj_set_x(ui_Screen2_Label4, -5);
    lv_obj_set_y(ui_Screen2_Label4, -7);
    lv_obj_set_align(ui_Screen2_Label4, LV_ALIGN_BOTTOM_RIGHT);
    lv_label_set_text(ui_Screen2_Label4, "|10:10");
    lv_obj_set_style_text_color(ui_Screen2_Label4, lv_color_hex(0x00FF00), LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_opa(ui_Screen2_Label4, 255, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_align(ui_Screen2_Label4, LV_TEXT_ALIGN_CENTER, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_font(ui_Screen2_Label4, &ui_font_InterMedium10, LV_PART_MAIN | LV_STATE_DEFAULT);

    ui_Screen2_Image2 = lv_img_create(ui_Screen2);
    lv_img_set_src(ui_Screen2_Image2, &ui_img_vector_png);
    lv_obj_set_width(ui_Screen2_Image2, 20);
    lv_obj_set_height(ui_Screen2_Image2, 20);
    lv_obj_set_x(ui_Screen2_Image2, -6);
    lv_obj_set_y(ui_Screen2_Image2, 76);
    lv_obj_set_align(ui_Screen2_Image2, LV_ALIGN_TOP_RIGHT);
    lv_obj_add_flag(ui_Screen2_Image2, LV_OBJ_FLAG_ADV_HITTEST);     /// Flags
    lv_obj_clear_flag(ui_Screen2_Image2, LV_OBJ_FLAG_SCROLLABLE);      /// Flags
    lv_img_set_pivot(ui_Screen2_Image2, 1, 0);
    lv_img_set_zoom(ui_Screen2_Image2, 320);

    ui_Destination = lv_label_create(ui_Screen2);
    lv_obj_set_width(ui_Destination, 200);
    lv_obj_set_height(ui_Destination, 14);
    lv_obj_set_x(ui_Destination, 7);
    lv_obj_set_y(ui_Destination, -7);
    lv_obj_set_align(ui_Destination, LV_ALIGN_BOTTOM_LEFT);
    lv_label_set_text(ui_Destination, "JL. Tujuan Kita");
    lv_obj_set_style_text_color(ui_Destination, lv_color_hex(0x00FF00), LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_opa(ui_Destination, 255, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_align(ui_Destination, LV_TEXT_ALIGN_LEFT, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_font(ui_Destination, &ui_font_InterMedium10, LV_PART_MAIN | LV_STATE_DEFAULT);

    ui_NextM = lv_label_create(ui_Screen2);
    lv_obj_set_width(ui_NextM, LV_SIZE_CONTENT);   /// 1
    lv_obj_set_height(ui_NextM, LV_SIZE_CONTENT);    /// 1
    lv_obj_set_x(ui_NextM, 69);
    lv_obj_set_y(ui_NextM, 26);
    lv_label_set_text(ui_NextM, "100 M");
    lv_obj_set_style_text_color(ui_NextM, lv_color_hex(0x00FF00), LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_opa(ui_NextM, 255, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_align(ui_NextM, LV_TEXT_ALIGN_AUTO, LV_PART_MAIN | LV_STATE_DEFAULT);
    lv_obj_set_style_text_font(ui_NextM, &ui_font_InterExtraBold18, LV_PART_MAIN | LV_STATE_DEFAULT);

    ui_Screen2_Image3 = lv_img_create(ui_Screen2);
    lv_img_set_src(ui_Screen2_Image3, &ui_img_whatsapp_png);
    lv_obj_set_width(ui_Screen2_Image3, LV_SIZE_CONTENT);   /// 1
    lv_obj_set_height(ui_Screen2_Image3, LV_SIZE_CONTENT);    /// 1
    lv_obj_set_x(ui_Screen2_Image3, -6);
    lv_obj_set_y(ui_Screen2_Image3, 38);
    lv_obj_set_align(ui_Screen2_Image3, LV_ALIGN_TOP_RIGHT);
    lv_obj_add_flag(ui_Screen2_Image3, LV_OBJ_FLAG_ADV_HITTEST);     /// Flags
    lv_obj_clear_flag(ui_Screen2_Image3, LV_OBJ_FLAG_SCROLLABLE);      /// Flags

    ui_Screen2_Image4 = lv_img_create(ui_Screen2);
    lv_img_set_src(ui_Screen2_Image4, &ui_img_zap_png);
    lv_obj_set_width(ui_Screen2_Image4, LV_SIZE_CONTENT);   /// 1
    lv_obj_set_height(ui_Screen2_Image4, LV_SIZE_CONTENT);    /// 1
    lv_obj_set_x(ui_Screen2_Image4, -36);
    lv_obj_set_y(ui_Screen2_Image4, 6);
    lv_obj_set_align(ui_Screen2_Image4, LV_ALIGN_TOP_RIGHT);
    lv_obj_add_flag(ui_Screen2_Image4, LV_OBJ_FLAG_ADV_HITTEST);     /// Flags
    lv_obj_clear_flag(ui_Screen2_Image4, LV_OBJ_FLAG_SCROLLABLE);      /// Flags

}

void ui_init(void)
{
    lv_disp_t * dispp = lv_disp_get_default();
    lv_theme_t * theme = lv_theme_default_init(dispp, lv_palette_main(LV_PALETTE_BLUE), lv_palette_main(LV_PALETTE_RED),
                                               false, LV_FONT_DEFAULT);
    lv_disp_set_theme(dispp, theme);
    ui_Screen1_screen_init();
    ui_Screen2_screen_init();
    lv_disp_load_scr(ui_Screen1);
}
