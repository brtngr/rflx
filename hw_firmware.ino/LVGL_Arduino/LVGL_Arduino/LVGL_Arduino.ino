#include <bluefruit.h>
#include <string>

#include <lvgl.h>
#include <TFT_eSPI.h>
#include <ArduinoJson.h>
#include "ui.h"

/*Change to your screen resolution*/
static const uint16_t screenWidth = 240;
static const uint16_t screenHeight = 135;

static lv_disp_draw_buf_t draw_buf;
static lv_color_t buf[screenWidth * screenHeight / 10];

char *payload = new char[8192];
int last_pointer = 0;
volatile bool new_payload = true;

TFT_eSPI tft = TFT_eSPI(); /* TFT instance */
#if LV_USE_LOG != 0
/* Serial debugging */
void my_print(const char *buf) {
  Serial.printf(buf);
  Serial.flush();
}
#endif

// max concurrent connections supported by this example
#define MAX_PRPH_CONNECTION 1
uint8_t connection_count = 0;

/* Nordic Led-Button Service (LBS)
 * LBS Service: 00001523-1212-EFDE-1523-785FEABCD123
 * LBS Button : 00001524-1212-EFDE-1523-785FEABCD123
 * LBS LED    : 00001525-1212-EFDE-1523-785FEABCD123
 */

const uint8_t LBS_UUID_SERVICE[] = {
  0x23, 0xD1, 0xBC, 0xEA, 0x5F, 0x78, 0x23, 0x15,
  0xDE, 0xEF, 0x12, 0x12, 0x23, 0x15, 0x00, 0x01
};

const uint8_t LBS_UUID_CHR_BUTTON[] = {
  0x23, 0xD1, 0xBC, 0xEA, 0x5F, 0x78, 0x23, 0x15,
  0xDE, 0xEF, 0x12, 0x12, 0x24, 0x15, 0x00, 0x01
};

const uint8_t LBS_UUID_CHR_LED[] = {
  0x23, 0xD1, 0xBC, 0xEA, 0x5F, 0x78, 0x23, 0x15,
  0xDE, 0xEF, 0x12, 0x12, 0x25, 0x15, 0x00, 0x01
};

BLEService lbs(LBS_UUID_SERVICE);
BLECharacteristic lsbLED(LBS_UUID_CHR_LED);

void setup() {

  Serial.begin(115200);
  //while ( !Serial ) delay(10);   // for nrf52840 with native usb
  memset(payload, 0x00, 8192);

  Serial.println("Reflex");
  Serial.println("------------------------------\n");

  // Initialize Bluefruit with max concurrent connections as Peripheral = MAX_PRPH_CONNECTION, Central = 0
  Serial.println("Initialise the Bluefruit nRF52 module");
  //Bluefruit.configPrphBandwidth(BANDWIDTH_MAX);
  Bluefruit.begin(MAX_PRPH_CONNECTION, 0);
  Bluefruit.setName("Reflex Add-On");
  Bluefruit.Periph.setConnectCallback(connect_callback);
  Bluefruit.Periph.setDisconnectCallback(disconnect_callback);

  // Setup the LED-Button service using
  Serial.println("Configuring the LED-Button Service");

  // Note: You must call .begin() on the BLEService before calling .begin() on
  // any characteristic(s) within that service definition.. Calling .begin() on
  // a BLECharacteristic will cause it to be added to the last BLEService that
  // was 'begin()'ed!
  lbs.begin();

  // Configure the LED characteristic
  // Properties = Read + Write
  // Permission = Open to read, Open to write
  // Fixed Len  = 1 (LED state)
  lsbLED.setProperties(CHR_PROPS_WRITE_WO_RESP);
  lsbLED.setPermission(SECMODE_OPEN, SECMODE_OPEN);
  //lsbLED.setFixedLen(1);
  lsbLED.begin();
  //lsbLED.write8(0x01); // led = on when connected

  lsbLED.setWriteCallback(led_write_callback);

  // Setup the advertising packet(s)
  Serial.println("Setting up the advertising");
  startAdv();

  //Setup display
  Serial.println("Setup display start");
  lv_init();

#if LV_USE_LOG != 0
  lv_log_register_print_cb(my_print); /* register print function for debugging */
#endif

  tft.begin();        /* TFT init */
  tft.setRotation(1); /* Landscape orientation, flipped */

  lv_disp_draw_buf_init(&draw_buf, buf, NULL, screenWidth * screenHeight / 10);

  /*Initialize the display*/
  static lv_disp_drv_t disp_drv;
  lv_disp_drv_init(&disp_drv);
  /*Change the following line to your display resolution*/
  disp_drv.hor_res = screenWidth;
  disp_drv.ver_res = screenHeight;
  disp_drv.flush_cb = my_disp_flush;
  disp_drv.draw_buf = &draw_buf;
  lv_disp_drv_register(&disp_drv);
  ui_init();

  Serial.println("Setup display done");
}

void my_disp_flush(lv_disp_drv_t *disp, const lv_area_t *area, lv_color_t *color_p) {
  uint32_t w = (area->x2 - area->x1 + 1);
  uint32_t h = (area->y2 - area->y1 + 1);

  tft.startWrite();
  tft.setAddrWindow(area->x1, area->y1, w, h);
  tft.pushColors((uint16_t *)&color_p->full, w * h, true);
  tft.endWrite();

  lv_disp_flush_ready(disp);
}

void startAdv(void) {
  // Advertising packet
  Bluefruit.Advertising.addFlags(BLE_GAP_ADV_FLAGS_LE_ONLY_GENERAL_DISC_MODE);
  Bluefruit.Advertising.addTxPower();

  // Include HRM Service UUID
  Bluefruit.Advertising.addService(lbs);

  // Secondary Scan Response packet (optional)
  // Since there is no room for 'Name' in Advertising packet
  Bluefruit.ScanResponse.addName();

  /* Start Advertising
   * - Enable auto advertising if disconnected
   * - Interval:  fast mode = 20 ms, slow mode = 152.5 ms
   * - Timeout for fast mode is 30 seconds
   * - Start(timeout) with timeout = 0 will advertise forever (until connected)
   * 
   * For recommended advertising interval
   * https://developer.apple.com/library/content/qa/qa1931/_index.html   
   */
  Bluefruit.Advertising.restartOnDisconnect(true);
  Bluefruit.Advertising.setInterval(32, 244);  // in unit of 0.625 ms
  Bluefruit.Advertising.setFastTimeout(30);    // number of seconds in fast mode
  Bluefruit.Advertising.addAppearance(448);
  Bluefruit.Advertising.start(0);  // 0 = Don't stop advertising after n seconds
}

void led_write_callback(uint16_t conn_hdl, BLECharacteristic *chr, uint8_t *data, uint16_t len) {
  if (new_payload) {
    //memset(payload, '\0', strlen(payload));
    memset(payload, 0x00, 8192);
    new_payload = false;
  }

  for (int i = 0; i < len; i++) {
    if ((char)data[i] == '\0') {
      Serial.println(last_pointer + len);
      Serial.println(payload);

      
      StaticJsonDocument<200> doc;
      DeserializationError error = deserializeJson(doc, payload);

      // Test if parsing succeeds.
      if (error) {
        Serial.print(F("deserializeJson() failed: "));
        Serial.println(error.f_str());
        return;
      }

      lv_label_set_text(ui_NextTurn, doc["destination"].as<const char *>());
      lv_label_set_text(ui_TimeDistance, doc["eta"].as<const char *>());
      lv_label_set_text(ui_Destination, doc["next_move"].as<const char *>());
      lv_label_set_text(ui_NextM, doc["next_distance"].as<const char *>());
      lv_label_set_text(ui_Screen2_Label4, doc["time"].as<const char *>());

      //close the process
      new_payload = true;
      last_pointer = 0;

    } else {
      payload[i + last_pointer] = (char)data[i];
      //Serial.print(i+last_pointer);
      //Serial.print(",");
      //Serial.println((char)data[i]);
      if (i == len - 1) {
        last_pointer = len + last_pointer;
      }
    }
  }
}

void loop() {
  lv_timer_handler(); /* let the GUI do its work */
  delay(5);
}

// callback invoked when central connects
void connect_callback(uint16_t conn_handle) {
  (void)conn_handle;

  //setLED(true);
  lv_scr_load(ui_Screen2);
  lsbLED.write8(0x01);

  connection_count++;
  Serial.print("Connection count: ");
  Serial.println(connection_count);

  // Keep advertising if not reaching max
  if (connection_count < MAX_PRPH_CONNECTION) {
    Serial.println("Keep advertising");
    Bluefruit.Advertising.start(0);
  }
}

/**
 * Callback invoked when a connection is dropped
 * @param conn_handle connection where this event happens
 * @param reason is a BLE_HCI_STATUS_CODE which can be found in ble_hci.h
 */
void disconnect_callback(uint16_t conn_handle, uint8_t reason) {
  (void)conn_handle;
  (void)reason;

  //setLED(false);
  lsbLED.write8(0x00);

  Serial.println();
  Serial.print("Disconnected, reason = 0x");
  Serial.println(reason, HEX);

  connection_count--;
  lv_scr_load(ui_Screen1);
}