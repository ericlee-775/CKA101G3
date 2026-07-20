
-- 建立資料庫
drop database IF EXISTS Farmily;
CREATE DATABASE IF NOT EXISTS Farmily CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Farmily;

-- ==========================================
-- 前置/獨立設定表 (無 FK 相依)
-- ==========================================

-- 8. 地址參照
CREATE TABLE CITY_DISTRICT (
                               district_id  INT         PRIMARY KEY AUTO_INCREMENT,
                               city_name    VARCHAR(50) NOT NULL,
                               city_eng     VARCHAR(50) NOT NULL,
                               dist_name    VARCHAR(50) NOT NULL,
                               dist_eng     VARCHAR(50) NOT NULL,
                               zipcode      SMALLINT    NOT NULL,
                               note         VARCHAR(50)
);
-- 參數
-- 重新插入全部資料（共 355 筆）
INSERT INTO CITY_DISTRICT (city_name, city_eng, dist_name, dist_eng, zipcode, note) VALUES
-- 臺北市
('臺北市', 'Taipei City', '中正區', 'Zhongzheng Dist.', 100, NULL),
('臺北市', 'Taipei City', '大同區', 'Datong Dist.', 103, NULL),
('臺北市', 'Taipei City', '中山區', 'Zhongshan Dist.', 104, NULL),
('臺北市', 'Taipei City', '松山區', 'Songshan Dist.', 105, NULL),
('臺北市', 'Taipei City', '大安區', 'Da''an Dist.', 106, NULL),
('臺北市', 'Taipei City', '萬華區', 'Wanhua Dist.', 108, NULL),
('臺北市', 'Taipei City', '信義區', 'Xinyi Dist.', 110, NULL),
('臺北市', 'Taipei City', '士林區', 'Shilin Dist.', 111, NULL),
('臺北市', 'Taipei City', '北投區', 'Beitou Dist.', 112, NULL),
('臺北市', 'Taipei City', '內湖區', 'Neihu Dist.', 114, NULL),
('臺北市', 'Taipei City', '南港區', 'Nangang Dist.', 115, NULL),
('臺北市', 'Taipei City', '文山區', 'Wenshan Dist.', 116, NULL),
-- 基隆市
('基隆市', 'Keelung City', '仁愛區', 'Ren''ai Dist.', 200, NULL),
('基隆市', 'Keelung City', '信義區', 'Xinyi Dist.', 201, NULL),
('基隆市', 'Keelung City', '中正區', 'Zhongzheng Dist.', 202, NULL),
('基隆市', 'Keelung City', '中山區', 'Zhongshan Dist.', 203, NULL),
('基隆市', 'Keelung City', '安樂區', 'Anle Dist.', 204, NULL),
('基隆市', 'Keelung City', '暖暖區', 'Nuannuan Dist.', 205, NULL),
('基隆市', 'Keelung City', '七堵區', 'Qidu Dist.', 206, NULL),
-- 新北市
('新北市', 'New Taipei City', '萬里區', 'Wanli Dist.', 207, NULL),
('新北市', 'New Taipei City', '金山區', 'Jinshan Dist.', 208, NULL),
('新北市', 'New Taipei City', '板橋區', 'Banqiao Dist.', 220, NULL),
('新北市', 'New Taipei City', '汐止區', 'Xizhi Dist.', 221, NULL),
('新北市', 'New Taipei City', '深坑區', 'Shenkeng Dist.', 222, NULL),
('新北市', 'New Taipei City', '石碇區', 'Shiding Dist.', 223, NULL),
('新北市', 'New Taipei City', '瑞芳區', 'Ruifang Dist.', 224, NULL),
('新北市', 'New Taipei City', '平溪區', 'Pingxi Dist.', 226, NULL),
('新北市', 'New Taipei City', '雙溪區', 'Shuangxi Dist.', 227, NULL),
('新北市', 'New Taipei City', '貢寮區', 'Gongliao Dist.', 228, NULL),
('新北市', 'New Taipei City', '新店區', 'Xindian Dist.', 231, NULL),
('新北市', 'New Taipei City', '坪林區', 'Pinglin Dist.', 232, NULL),
('新北市', 'New Taipei City', '烏來區', 'Wulai Dist.', 233, NULL),
('新北市', 'New Taipei City', '永和區', 'Yonghe Dist.', 234, NULL),
('新北市', 'New Taipei City', '中和區', 'Zhonghe Dist.', 235, NULL),
('新北市', 'New Taipei City', '土城區', 'Tucheng Dist.', 236, NULL),
('新北市', 'New Taipei City', '三峽區', 'Sanxia Dist.', 237, NULL),
('新北市', 'New Taipei City', '樹林區', 'Shulin Dist.', 238, NULL),
('新北市', 'New Taipei City', '鶯歌區', 'Yingge Dist.', 239, NULL),
('新北市', 'New Taipei City', '三重區', 'Sanchong Dist.', 241, NULL),
('新北市', 'New Taipei City', '新莊區', 'Xinzhuang Dist.', 242, NULL),
('新北市', 'New Taipei City', '泰山區', 'Taishan Dist.', 243, NULL),
('新北市', 'New Taipei City', '林口區', 'Linkou Dist.', 244, NULL),
('新北市', 'New Taipei City', '蘆洲區', 'Luzhou Dist.', 247, NULL),
('新北市', 'New Taipei City', '五股區', 'Wugu Dist.', 248, NULL),
('新北市', 'New Taipei City', '八里區', 'Bali Dist.', 249, NULL),
('新北市', 'New Taipei City', '淡水區', 'Tamsui Dist.', 251, NULL),
('新北市', 'New Taipei City', '三芝區', 'Sanzhi Dist.', 252, NULL),
('新北市', 'New Taipei City', '石門區', 'Shimen Dist.', 253, NULL),
-- 連江縣
('連江縣', 'Lienchiang County', '南竿鄉', 'Nangan Township', 209, NULL),
('連江縣', 'Lienchiang County', '北竿鄉', 'Beigan Township', 210, NULL),
('連江縣', 'Lienchiang County', '莒光鄉', 'Juguang Township', 211, NULL),
('連江縣', 'Lienchiang County', '東引鄉', 'Dongyin Township', 212, NULL),
-- 宜蘭縣
('宜蘭縣', 'Yilan County', '宜蘭市', 'Yilan City', 260, NULL),
('宜蘭縣', 'Yilan County', '頭城鎮', 'Toucheng Township', 261, NULL),
('宜蘭縣', 'Yilan County', '礁溪鄉', 'Jiaoxi Township', 262, NULL),
('宜蘭縣', 'Yilan County', '壯圍鄉', 'Zhuangwei Township', 263, NULL),
('宜蘭縣', 'Yilan County', '員山鄉', 'Yuanshan Township', 264, NULL),
('宜蘭縣', 'Yilan County', '羅東鎮', 'Luodong Township', 265, NULL),
('宜蘭縣', 'Yilan County', '三星鄉', 'Sanxing Township', 266, NULL),
('宜蘭縣', 'Yilan County', '大同鄉', 'Datong Township', 267, NULL),
('宜蘭縣', 'Yilan County', '五結鄉', 'Wujie Township', 268, NULL),
('宜蘭縣', 'Yilan County', '冬山鄉', 'Dongshan Township', 269, NULL),
('宜蘭縣', 'Yilan County', '蘇澳鎮', 'Su''ao Township', 270, NULL),
('宜蘭縣', 'Yilan County', '南澳鄉', 'Nan''ao Township', 272, NULL),
('宜蘭縣', 'Yilan County', '釣魚臺', 'Diaoyutai', 290, NULL),
-- 新竹市
('新竹市', 'Hsinchu City', '東區', 'East Dist.', 300, '新竹市三區共用3碼300，以district_id區分'),
('新竹市', 'Hsinchu City', '北區', 'North Dist.', 300, '新竹市三區共用3碼300，以district_id區分'),
('新竹市', 'Hsinchu City', '香山區', 'Xiangshan Dist.', 300, '新竹市三區共用3碼300，以district_id區分'),
-- 新竹縣
('新竹縣', 'Hsinchu County', '竹北市', 'Zhubei City', 302, NULL),
('新竹縣', 'Hsinchu County', '湖口鄉', 'Hukou Township', 303, NULL),
('新竹縣', 'Hsinchu County', '新豐鄉', 'Xinfeng Township', 304, NULL),
('新竹縣', 'Hsinchu County', '新埔鎮', 'Xinpu Township', 305, NULL),
('新竹縣', 'Hsinchu County', '關西鎮', 'Guanxi Township', 306, NULL),
('新竹縣', 'Hsinchu County', '芎林鄉', 'Qionglin Township', 307, NULL),
('新竹縣', 'Hsinchu County', '寶山鄉', 'Baoshan Township', 308, NULL),
('新竹縣', 'Hsinchu County', '竹東鎮', 'Zhudong Township', 310, NULL),
('新竹縣', 'Hsinchu County', '五峰鄉', 'Wufeng Township', 311, NULL),
('新竹縣', 'Hsinchu County', '橫山鄉', 'Hengshan Township', 312, NULL),
('新竹縣', 'Hsinchu County', '尖石鄉', 'Jianshi Township', 313, NULL),
('新竹縣', 'Hsinchu County', '北埔鄉', 'Beipu Township', 314, NULL),
('新竹縣', 'Hsinchu County', '峨眉鄉', 'Emei Township', 315, NULL),
-- 桃園市
('桃園市', 'Taoyuan City', '中壢區', 'Zhongli Dist.', 320, NULL),
('桃園市', 'Taoyuan City', '平鎮區', 'Pingzhen Dist.', 324, NULL),
('桃園市', 'Taoyuan City', '龍潭區', 'Longtan Dist.', 325, NULL),
('桃園市', 'Taoyuan City', '楊梅區', 'Yangmei Dist.', 326, NULL),
('桃園市', 'Taoyuan City', '新屋區', 'Xinwu Dist.', 327, NULL),
('桃園市', 'Taoyuan City', '觀音區', 'Guanyin Dist.', 328, NULL),
('桃園市', 'Taoyuan City', '桃園區', 'Taoyuan Dist.', 330, NULL),
('桃園市', 'Taoyuan City', '龜山區', 'Guishan Dist.', 333, NULL),
('桃園市', 'Taoyuan City', '八德區', 'Bade Dist.', 334, NULL),
('桃園市', 'Taoyuan City', '大溪區', 'Daxi Dist.', 335, NULL),
('桃園市', 'Taoyuan City', '復興區', 'Fuxing Dist.', 336, NULL),
('桃園市', 'Taoyuan City', '大園區', 'Dayuan Dist.', 337, NULL),
('桃園市', 'Taoyuan City', '蘆竹區', 'Luzhu Dist.', 338, NULL),
-- 苗栗縣
('苗栗縣', 'Miaoli County', '竹南鎮', 'Zhunan Township', 350, NULL),
('苗栗縣', 'Miaoli County', '頭份市', 'Toufen City', 351, NULL),
('苗栗縣', 'Miaoli County', '三灣鄉', 'Sanwan Township', 352, NULL),
('苗栗縣', 'Miaoli County', '南庄鄉', 'Nanzhuang Township', 353, NULL),
('苗栗縣', 'Miaoli County', '獅潭鄉', 'Shitan Township', 354, NULL),
('苗栗縣', 'Miaoli County', '後龍鎮', 'Houlong Township', 356, NULL),
('苗栗縣', 'Miaoli County', '通霄鎮', 'Tongxiao Township', 357, NULL),
('苗栗縣', 'Miaoli County', '苑裡鎮', 'Yuanli Township', 358, NULL),
('苗栗縣', 'Miaoli County', '苗栗市', 'Miaoli City', 360, NULL),
('苗栗縣', 'Miaoli County', '造橋鄉', 'Zaoqiao Township', 361, NULL),
('苗栗縣', 'Miaoli County', '頭屋鄉', 'Touwu Township', 362, NULL),
('苗栗縣', 'Miaoli County', '公館鄉', 'Gongguan Township', 363, NULL),
('苗栗縣', 'Miaoli County', '大湖鄉', 'Dahu Township', 364, NULL),
('苗栗縣', 'Miaoli County', '泰安鄉', 'Tai''an Township', 365, NULL),
('苗栗縣', 'Miaoli County', '銅鑼鄉', 'Tongluo Township', 366, NULL),
('苗栗縣', 'Miaoli County', '三義鄉', 'Sanyi Township', 367, NULL),
('苗栗縣', 'Miaoli County', '西湖鄉', 'Xihu Township', 368, NULL),
('苗栗縣', 'Miaoli County', '卓蘭鎮', 'Zhuolan Township', 369, NULL),
-- 臺中市
('臺中市', 'Taichung City', '中區', 'Central Dist.', 400, NULL),
('臺中市', 'Taichung City', '東區', 'East Dist.', 401, NULL),
('臺中市', 'Taichung City', '南區', 'South Dist.', 402, NULL),
('臺中市', 'Taichung City', '西區', 'West Dist.', 403, NULL),
('臺中市', 'Taichung City', '北區', 'North Dist.', 404, NULL),
('臺中市', 'Taichung City', '北屯區', 'Beitun Dist.', 406, NULL),
('臺中市', 'Taichung City', '西屯區', 'Xitun Dist.', 407, NULL),
('臺中市', 'Taichung City', '南屯區', 'Nantun Dist.', 408, NULL),
('臺中市', 'Taichung City', '太平區', 'Taiping Dist.', 411, NULL),
('臺中市', 'Taichung City', '大里區', 'Dali Dist.', 412, NULL),
('臺中市', 'Taichung City', '霧峰區', 'Wufeng Dist.', 413, NULL),
('臺中市', 'Taichung City', '烏日區', 'Wuri Dist.', 414, NULL),
('臺中市', 'Taichung City', '豐原區', 'Fengyuan Dist.', 420, NULL),
('臺中市', 'Taichung City', '后里區', 'Houli Dist.', 421, NULL),
('臺中市', 'Taichung City', '石岡區', 'Shigang Dist.', 422, NULL),
('臺中市', 'Taichung City', '東勢區', 'Dongshi Dist.', 423, NULL),
('臺中市', 'Taichung City', '和平區', 'Heping Dist.', 424, NULL),
('臺中市', 'Taichung City', '新社區', 'Xinshe Dist.', 426, NULL),
('臺中市', 'Taichung City', '潭子區', 'Tanzi Dist.', 427, NULL),
('臺中市', 'Taichung City', '大雅區', 'Daya Dist.', 428, NULL),
('臺中市', 'Taichung City', '神岡區', 'Shengang Dist.', 429, NULL),
('臺中市', 'Taichung City', '大肚區', 'Dadu Dist.', 432, NULL),
('臺中市', 'Taichung City', '沙鹿區', 'Shalu Dist.', 433, NULL),
('臺中市', 'Taichung City', '龍井區', 'Longjing Dist.', 434, NULL),
('臺中市', 'Taichung City', '梧棲區', 'Wuqi Dist.', 435, NULL),
('臺中市', 'Taichung City', '清水區', 'Qingshui Dist.', 436, NULL),
('臺中市', 'Taichung City', '大甲區', 'Dajia Dist.', 437, NULL),
('臺中市', 'Taichung City', '外埔區', 'Waipu Dist.', 438, NULL),
('臺中市', 'Taichung City', '大安區', 'Da''an Dist.', 439, NULL),
-- 彰化縣
('彰化縣', 'Changhua County', '彰化市', 'Changhua City', 500, NULL),
('彰化縣', 'Changhua County', '芬園鄉', 'Fenyuan Township', 502, NULL),
('彰化縣', 'Changhua County', '花壇鄉', 'Huatan Township', 503, NULL),
('彰化縣', 'Changhua County', '秀水鄉', 'Xiushui Township', 504, NULL),
('彰化縣', 'Changhua County', '鹿港鎮', 'Lukang Township', 505, NULL),
('彰化縣', 'Changhua County', '福興鄉', 'Fuxing Township', 506, NULL),
('彰化縣', 'Changhua County', '線西鄉', 'Xianxi Township', 507, NULL),
('彰化縣', 'Changhua County', '和美鎮', 'Hemei Township', 508, NULL),
('彰化縣', 'Changhua County', '伸港鄉', 'Shengang Township', 509, NULL),
('彰化縣', 'Changhua County', '員林市', 'Yuanlin City', 510, NULL),
('彰化縣', 'Changhua County', '社頭鄉', 'Shetou Township', 511, NULL),
('彰化縣', 'Changhua County', '永靖鄉', 'Yongjing Township', 512, NULL),
('彰化縣', 'Changhua County', '埔心鄉', 'Puxin Township', 513, NULL),
('彰化縣', 'Changhua County', '溪湖鎮', 'Xihu Township', 514, NULL),
('彰化縣', 'Changhua County', '大村鄉', 'Dacun Township', 515, NULL),
('彰化縣', 'Changhua County', '埔鹽鄉', 'Puyan Township', 516, NULL),
('彰化縣', 'Changhua County', '田中鎮', 'Tianzhong Township', 520, NULL),
('彰化縣', 'Changhua County', '北斗鎮', 'Beidou Township', 521, NULL),
('彰化縣', 'Changhua County', '田尾鄉', 'Tianwei Township', 522, NULL),
('彰化縣', 'Changhua County', '埤頭鄉', 'Pitou Township', 523, NULL),
('彰化縣', 'Changhua County', '溪州鄉', 'Xizhou Township', 524, NULL),
('彰化縣', 'Changhua County', '竹塘鄉', 'Zhutang Township', 525, NULL),
('彰化縣', 'Changhua County', '二林鎮', 'Erlin Township', 526, NULL),
('彰化縣', 'Changhua County', '大城鄉', 'Dacheng Township', 527, NULL),
('彰化縣', 'Changhua County', '芳苑鄉', 'Fangyuan Township', 528, NULL),
('彰化縣', 'Changhua County', '二水鄉', 'Ershui Township', 530, NULL),
-- 南投縣
('南投縣', 'Nantou County', '南投市', 'Nantou City', 540, NULL),
('南投縣', 'Nantou County', '中寮鄉', 'Zhongliao Township', 541, NULL),
('南投縣', 'Nantou County', '草屯鎮', 'Caotun Township', 542, NULL),
('南投縣', 'Nantou County', '國姓鄉', 'Guoxing Township', 544, NULL),
('南投縣', 'Nantou County', '埔里鎮', 'Puli Township', 545, NULL),
('南投縣', 'Nantou County', '仁愛鄉', 'Ren''ai Township', 546, NULL),
('南投縣', 'Nantou County', '名間鄉', 'Mingjian Township', 551, NULL),
('南投縣', 'Nantou County', '集集鎮', 'Jiji Township', 552, NULL),
('南投縣', 'Nantou County', '水里鄉', 'Shuili Township', 553, NULL),
('南投縣', 'Nantou County', '魚池鄉', 'Yuchi Township', 555, NULL),
('南投縣', 'Nantou County', '信義鄉', 'Xinyi Township', 556, NULL),
('南投縣', 'Nantou County', '竹山鎮', 'Zhushan Township', 557, NULL),
('南投縣', 'Nantou County', '鹿谷鄉', 'Lugu Township', 558, NULL),
-- 嘉義市
('嘉義市', 'Chiayi City', '西區', 'West Dist.', 600, '嘉義市兩區共用3碼600，以district_id區分'),
('嘉義市', 'Chiayi City', '東區', 'East Dist.', 600, '嘉義市兩區共用3碼600，以district_id區分'),
-- 嘉義縣
('嘉義縣', 'Chiayi County', '番路鄉', 'Fanlu Township', 602, NULL),
('嘉義縣', 'Chiayi County', '梅山鄉', 'Meishan Township', 603, NULL),
('嘉義縣', 'Chiayi County', '竹崎鄉', 'Zhuqi Township', 604, NULL),
('嘉義縣', 'Chiayi County', '阿里山鄉', 'Alishan Township', 605, NULL),
('嘉義縣', 'Chiayi County', '中埔鄉', 'Zhongpu Township', 606, NULL),
('嘉義縣', 'Chiayi County', '大埔鄉', 'Dapu Township', 607, NULL),
('嘉義縣', 'Chiayi County', '水上鄉', 'Shuishang Township', 608, NULL),
('嘉義縣', 'Chiayi County', '鹿草鄉', 'Lucao Township', 611, NULL),
('嘉義縣', 'Chiayi County', '太保市', 'Taibao City', 612, NULL),
('嘉義縣', 'Chiayi County', '朴子市', 'Puzi City', 613, NULL),
('嘉義縣', 'Chiayi County', '東石鄉', 'Dongshi Township', 614, NULL),
('嘉義縣', 'Chiayi County', '六腳鄉', 'Liujiao Township', 615, NULL),
('嘉義縣', 'Chiayi County', '新港鄉', 'Xingang Township', 616, NULL),
('嘉義縣', 'Chiayi County', '民雄鄉', 'Minxiong Township', 621, NULL),
('嘉義縣', 'Chiayi County', '大林鎮', 'Dalin Township', 622, NULL),
('嘉義縣', 'Chiayi County', '溪口鄉', 'Xikou Township', 623, NULL),
('嘉義縣', 'Chiayi County', '義竹鄉', 'Yizhu Township', 624, NULL),
('嘉義縣', 'Chiayi County', '布袋鎮', 'Budai Township', 625, NULL),
-- 雲林縣
('雲林縣', 'Yunlin County', '斗南鎮', 'Dounan Township', 630, NULL),
('雲林縣', 'Yunlin County', '大埤鄉', 'Dapi Township', 631, NULL),
('雲林縣', 'Yunlin County', '虎尾鎮', 'Huwei Township', 632, NULL),
('雲林縣', 'Yunlin County', '土庫鎮', 'Tuku Township', 633, NULL),
('雲林縣', 'Yunlin County', '褒忠鄉', 'Baozhong Township', 634, NULL),
('雲林縣', 'Yunlin County', '東勢鄉', 'Dongshi Township', 635, NULL),
('雲林縣', 'Yunlin County', '臺西鄉', 'Taixi Township', 636, NULL),
('雲林縣', 'Yunlin County', '崙背鄉', 'Lunbei Township', 637, NULL),
('雲林縣', 'Yunlin County', '麥寮鄉', 'Mailiao Township', 638, NULL),
('雲林縣', 'Yunlin County', '斗六市', 'Douliu City', 640, NULL),
('雲林縣', 'Yunlin County', '林內鄉', 'Linnei Township', 643, NULL),
('雲林縣', 'Yunlin County', '古坑鄉', 'Gukeng Township', 646, NULL),
('雲林縣', 'Yunlin County', '莿桐鄉', 'Citong Township', 647, NULL),
('雲林縣', 'Yunlin County', '西螺鎮', 'Xiluo Township', 648, NULL),
('雲林縣', 'Yunlin County', '二崙鄉', 'Erlun Township', 649, NULL),
('雲林縣', 'Yunlin County', '北港鎮', 'Beigang Township', 651, NULL),
('雲林縣', 'Yunlin County', '水林鄉', 'Shuilin Township', 652, NULL),
('雲林縣', 'Yunlin County', '口湖鄉', 'Kouhu Township', 653, NULL),
('雲林縣', 'Yunlin County', '四湖鄉', 'Sihu Township', 654, NULL),
('雲林縣', 'Yunlin County', '元長鄉', 'Yuanchang Township', 655, NULL),
-- 臺南市
('臺南市', 'Tainan City', '中西區', 'West Central Dist.', 700, NULL),
('臺南市', 'Tainan City', '東區', 'East Dist.', 701, NULL),
('臺南市', 'Tainan City', '南區', 'South Dist.', 702, NULL),
('臺南市', 'Tainan City', '北區', 'North Dist.', 704, NULL),
('臺南市', 'Tainan City', '安平區', 'Anping Dist.', 708, NULL),
('臺南市', 'Tainan City', '安南區', 'Annan Dist.', 709, NULL),
('臺南市', 'Tainan City', '永康區', 'Yongkang Dist.', 710, NULL),
('臺南市', 'Tainan City', '歸仁區', 'Guiren Dist.', 711, NULL),
('臺南市', 'Tainan City', '新化區', 'Xinhua Dist.', 712, NULL),
('臺南市', 'Tainan City', '左鎮區', 'Zuozhen Dist.', 713, NULL),
('臺南市', 'Tainan City', '玉井區', 'Yujing Dist.', 714, NULL),
('臺南市', 'Tainan City', '楠西區', 'Nanxi Dist.', 715, NULL),
('臺南市', 'Tainan City', '南化區', 'Nanhua Dist.', 716, NULL),
('臺南市', 'Tainan City', '仁德區', 'Rende Dist.', 717, NULL),
('臺南市', 'Tainan City', '關廟區', 'Guanmiao Dist.', 718, NULL),
('臺南市', 'Tainan City', '龍崎區', 'Longqi Dist.', 719, NULL),
('臺南市', 'Tainan City', '官田區', 'Guantian Dist.', 720, NULL),
('臺南市', 'Tainan City', '麻豆區', 'Madou Dist.', 721, NULL),
('臺南市', 'Tainan City', '佳里區', 'Jiali Dist.', 722, NULL),
('臺南市', 'Tainan City', '西港區', 'Xigang Dist.', 723, NULL),
('臺南市', 'Tainan City', '七股區', 'Qigu Dist.', 724, NULL),
('臺南市', 'Tainan City', '將軍區', 'Jiangjun Dist.', 725, NULL),
('臺南市', 'Tainan City', '學甲區', 'Xuejia Dist.', 726, NULL),
('臺南市', 'Tainan City', '北門區', 'Beimen Dist.', 727, NULL),
('臺南市', 'Tainan City', '新營區', 'Xinying Dist.', 730, NULL),
('臺南市', 'Tainan City', '後壁區', 'Houbi Dist.', 731, NULL),
('臺南市', 'Tainan City', '白河區', 'Baihe Dist.', 732, NULL),
('臺南市', 'Tainan City', '東山區', 'Dongshan Dist.', 733, NULL),
('臺南市', 'Tainan City', '六甲區', 'Liujia Dist.', 734, NULL),
('臺南市', 'Tainan City', '下營區', 'Xiaying Dist.', 735, NULL),
('臺南市', 'Tainan City', '柳營區', 'Liuying Dist.', 736, NULL),
('臺南市', 'Tainan City', '鹽水區', 'Yanshui Dist.', 737, NULL),
('臺南市', 'Tainan City', '善化區', 'Shanhua Dist.', 741, NULL),
('臺南市', 'Tainan City', '大內區', 'Danei Dist.', 742, NULL),
('臺南市', 'Tainan City', '山上區', 'Shanshang Dist.', 743, NULL),
('臺南市', 'Tainan City', '新市區', 'Xinshi Dist.', 744, NULL),
('臺南市', 'Tainan City', '安定區', 'Anding Dist.', 745, NULL),
-- 高雄市
('高雄市', 'Kaohsiung City', '新興區', 'Xinxing Dist.', 800, NULL),
('高雄市', 'Kaohsiung City', '前金區', 'Qianjin Dist.', 801, NULL),
('高雄市', 'Kaohsiung City', '苓雅區', 'Lingya Dist.', 802, NULL),
('高雄市', 'Kaohsiung City', '鹽埕區', 'Yancheng Dist.', 803, NULL),
('高雄市', 'Kaohsiung City', '鼓山區', 'Gushan Dist.', 804, NULL),
('高雄市', 'Kaohsiung City', '旗津區', 'Qijin Dist.', 805, NULL),
('高雄市', 'Kaohsiung City', '前鎮區', 'Qianzhen Dist.', 806, NULL),
('高雄市', 'Kaohsiung City', '三民區', 'Sanmin Dist.', 807, NULL),
('高雄市', 'Kaohsiung City', '楠梓區', 'Nanzi Dist.', 811, NULL),
('高雄市', 'Kaohsiung City', '小港區', 'Xiaogang Dist.', 812, NULL),
('高雄市', 'Kaohsiung City', '左營區', 'Zuoying Dist.', 813, NULL),
('高雄市', 'Kaohsiung City', '仁武區', 'Renwu Dist.', 814, NULL),
('高雄市', 'Kaohsiung City', '大社區', 'Dashe Dist.', 815, NULL),
('高雄市', 'Kaohsiung City', '東沙群島', 'Dongsha Islands', 817, NULL),
('高雄市', 'Kaohsiung City', '南沙群島', 'Nansha Islands', 819, NULL),
('高雄市', 'Kaohsiung City', '岡山區', 'Gangshan Dist.', 820, NULL),
('高雄市', 'Kaohsiung City', '路竹區', 'Luzhu Dist.', 821, NULL),
('高雄市', 'Kaohsiung City', '阿蓮區', 'Alian Dist.', 822, NULL),
('高雄市', 'Kaohsiung City', '田寮區', 'Tianliao Dist.', 823, NULL),
('高雄市', 'Kaohsiung City', '燕巢區', 'Yanchao Dist.', 824, NULL),
('高雄市', 'Kaohsiung City', '橋頭區', 'Qiaotou Dist.', 825, NULL),
('高雄市', 'Kaohsiung City', '梓官區', 'Ziguan Dist.', 826, NULL),
('高雄市', 'Kaohsiung City', '彌陀區', 'Mituo Dist.', 827, NULL),
('高雄市', 'Kaohsiung City', '永安區', 'Yong''an Dist.', 828, NULL),
('高雄市', 'Kaohsiung City', '湖內區', 'Hunei Dist.', 829, NULL),
('高雄市', 'Kaohsiung City', '鳳山區', 'Fengshan Dist.', 830, NULL),
('高雄市', 'Kaohsiung City', '大寮區', 'Daliao Dist.', 831, NULL),
('高雄市', 'Kaohsiung City', '林園區', 'Linyuan Dist.', 832, NULL),
('高雄市', 'Kaohsiung City', '鳥松區', 'Niaosong Dist.', 833, NULL),
('高雄市', 'Kaohsiung City', '大樹區', 'Dashu Dist.', 840, NULL),
('高雄市', 'Kaohsiung City', '旗山區', 'Qishan Dist.', 842, NULL),
('高雄市', 'Kaohsiung City', '美濃區', 'Meinong Dist.', 843, NULL),
('高雄市', 'Kaohsiung City', '六龜區', 'Liugui Dist.', 844, NULL),
('高雄市', 'Kaohsiung City', '內門區', 'Neimen Dist.', 845, NULL),
('高雄市', 'Kaohsiung City', '杉林區', 'Shanlin Dist.', 846, NULL),
('高雄市', 'Kaohsiung City', '甲仙區', 'Jiaxian Dist.', 847, NULL),
('高雄市', 'Kaohsiung City', '桃源區', 'Taoyuan Dist.', 848, NULL),
('高雄市', 'Kaohsiung City', '那瑪夏區', 'Namaxia Dist.', 849, NULL),
('高雄市', 'Kaohsiung City', '茂林區', 'Maolin Dist.', 851, NULL),
('高雄市', 'Kaohsiung City', '茄萣區', 'Qieding Dist.', 852, NULL),
-- 澎湖縣
('澎湖縣', 'Penghu County', '馬公市', 'Magong City', 880, NULL),
('澎湖縣', 'Penghu County', '西嶼鄉', 'Xiyu Township', 881, NULL),
('澎湖縣', 'Penghu County', '望安鄉', 'Wang''an Township', 882, NULL),
('澎湖縣', 'Penghu County', '七美鄉', 'Qimei Township', 883, NULL),
('澎湖縣', 'Penghu County', '白沙鄉', 'Baisha Township', 884, NULL),
('澎湖縣', 'Penghu County', '湖西鄉', 'Huxi Township', 885, NULL),
-- 金門縣
('金門縣', 'Kinmen County', '金沙鎮', 'Jinsha Township', 890, NULL),
('金門縣', 'Kinmen County', '金湖鎮', 'Jinhu Township', 891, NULL),
('金門縣', 'Kinmen County', '金寧鄉', 'Jinning Township', 892, NULL),
('金門縣', 'Kinmen County', '金城鎮', 'Jincheng Township', 893, NULL),
('金門縣', 'Kinmen County', '烈嶼鄉', 'Lieyu Township', 894, NULL),
('金門縣', 'Kinmen County', '烏坵鄉', 'Wuqiu Township', 896, NULL),
-- 屏東縣
('屏東縣', 'Pingtung County', '屏東市', 'Pingtung City', 900, NULL),
('屏東縣', 'Pingtung County', '三地門鄉', 'Sandimen Township', 901, NULL),
('屏東縣', 'Pingtung County', '霧臺鄉', 'Wutai Township', 902, NULL),
('屏東縣', 'Pingtung County', '瑪家鄉', 'Majia Township', 903, NULL),
('屏東縣', 'Pingtung County', '九如鄉', 'Jiuru Township', 904, NULL),
('屏東縣', 'Pingtung County', '里港鄉', 'Ligang Township', 905, NULL),
('屏東縣', 'Pingtung County', '高樹鄉', 'Gaoshu Township', 906, NULL),
('屏東縣', 'Pingtung County', '鹽埔鄉', 'Yanpu Township', 907, NULL),
('屏東縣', 'Pingtung County', '長治鄉', 'Changzhi Township', 908, NULL),
('屏東縣', 'Pingtung County', '麟洛鄉', 'Linluo Township', 909, NULL),
('屏東縣', 'Pingtung County', '竹田鄉', 'Zhutian Township', 911, NULL),
('屏東縣', 'Pingtung County', '內埔鄉', 'Neipu Township', 912, NULL),
('屏東縣', 'Pingtung County', '萬丹鄉', 'Wandan Township', 913, NULL),
('屏東縣', 'Pingtung County', '潮州鎮', 'Chaozhou Township', 920, NULL),
('屏東縣', 'Pingtung County', '泰武鄉', 'Taiwu Township', 921, NULL),
('屏東縣', 'Pingtung County', '來義鄉', 'Laiyi Township', 922, NULL),
('屏東縣', 'Pingtung County', '萬巒鄉', 'Wanluan Township', 923, NULL),
('屏東縣', 'Pingtung County', '崁頂鄉', 'Kanding Township', 924, NULL),
('屏東縣', 'Pingtung County', '新埤鄉', 'Xinpi Township', 925, NULL),
('屏東縣', 'Pingtung County', '南州鄉', 'Nanzhou Township', 926, NULL),
('屏東縣', 'Pingtung County', '林邊鄉', 'Linbian Township', 927, NULL),
('屏東縣', 'Pingtung County', '東港鎮', 'Donggang Township', 928, NULL),
('屏東縣', 'Pingtung County', '琉球鄉', 'Liuqiu Township', 929, NULL),
('屏東縣', 'Pingtung County', '佳冬鄉', 'Jiadong Township', 931, NULL),
('屏東縣', 'Pingtung County', '新園鄉', 'Xinyuan Township', 932, NULL),
('屏東縣', 'Pingtung County', '枋寮鄉', 'Fangliao Township', 940, NULL),
('屏東縣', 'Pingtung County', '枋山鄉', 'Fangshan Township', 941, NULL),
('屏東縣', 'Pingtung County', '春日鄉', 'Chunri Township', 942, NULL),
('屏東縣', 'Pingtung County', '獅子鄉', 'Shizi Township', 943, NULL),
('屏東縣', 'Pingtung County', '車城鄉', 'Checheng Township', 944, NULL),
('屏東縣', 'Pingtung County', '牡丹鄉', 'Mudan Township', 945, NULL),
('屏東縣', 'Pingtung County', '恆春鎮', 'Hengchun Township', 946, NULL),
('屏東縣', 'Pingtung County', '滿州鄉', 'Manzhou Township', 947, NULL),
-- 臺東縣
('臺東縣', 'Taitung County', '臺東市', 'Taitung City', 950, NULL),
('臺東縣', 'Taitung County', '綠島鄉', 'Ludao Township', 951, NULL),
('臺東縣', 'Taitung County', '蘭嶼鄉', 'Lanyu Township', 952, NULL),
('臺東縣', 'Taitung County', '延平鄉', 'Yanping Township', 953, NULL),
('臺東縣', 'Taitung County', '卑南鄉', 'Beinan Township', 954, NULL),
('臺東縣', 'Taitung County', '鹿野鄉', 'Luye Township', 955, NULL),
('臺東縣', 'Taitung County', '關山鎮', 'Guanshan Township', 956, NULL),
('臺東縣', 'Taitung County', '海端鄉', 'Haiduan Township', 957, NULL),
('臺東縣', 'Taitung County', '池上鄉', 'Chishang Township', 958, NULL),
('臺東縣', 'Taitung County', '東河鄉', 'Donghe Township', 959, NULL),
('臺東縣', 'Taitung County', '成功鎮', 'Chenggong Township', 961, NULL),
('臺東縣', 'Taitung County', '長濱鄉', 'Changbin Township', 962, NULL),
('臺東縣', 'Taitung County', '太麻里鄉', 'Taimali Township', 963, NULL),
('臺東縣', 'Taitung County', '金峰鄉', 'Jinfeng Township', 964, NULL),
('臺東縣', 'Taitung County', '達仁鄉', 'Daren Township', 966, NULL),
('臺東縣', 'Taitung County', '大武鄉', 'Dawu Township', 965, NULL),
-- 花蓮縣（共 13 筆）
('花蓮縣', 'Hualien County', '花蓮市', 'Hualien City', 970, NULL),
('花蓮縣', 'Hualien County', '新城鄉', 'Xincheng Township', 971, NULL),
('花蓮縣', 'Hualien County', '秀林鄉', 'Xiulin Township', 972, NULL),
('花蓮縣', 'Hualien County', '吉安鄉', 'Ji''an Township', 973, NULL),
('花蓮縣', 'Hualien County', '壽豐鄉', 'Shoufeng Township', 974, NULL),
('花蓮縣', 'Hualien County', '鳳林鎮', 'Fenglin Township', 975, NULL),
('花蓮縣', 'Hualien County', '光復鄉', 'Guangfu Township', 976, NULL),
('花蓮縣', 'Hualien County', '豐濱鄉', 'Fengbin Township', 977, NULL),
('花蓮縣', 'Hualien County', '瑞穗鄉', 'Ruisui Township', 978, NULL),
('花蓮縣', 'Hualien County', '萬榮鄉', 'Wanrong Township', 979, NULL),
('花蓮縣', 'Hualien County', '玉里鎮', 'Yuli Township', 981, NULL),
('花蓮縣', 'Hualien County', '卓溪鄉', 'Zhuoxi Township', 982, NULL),
('花蓮縣', 'Hualien County', '富里鄉', 'Fuli Township', 983, NULL);



-- 1. 會員系統 - 一般會員
CREATE TABLE USER (
                      user_id          INT          PRIMARY KEY AUTO_INCREMENT,
                      email            VARCHAR(255)  UNIQUE,
                      district_id      INT,
                      user_address     VARCHAR(100),
                      email_verified   BOOLEAN,
                      password         VARCHAR(255),
                      user_created_at  DATETIME,
                      user_name        VARCHAR(100),
                      user_nickname    VARCHAR(100),
                      birthday         DATE,
                      user_phone_num   VARCHAR(50),
                      user_status      ENUM('ACTIVE', 'WARNED', 'SUSPENDED', 'DELETED') DEFAULT 'ACTIVE',
                      monthly_spending INT          NOT NULL DEFAULT 0,
                      auth_provider    ENUM('LOCAL', 'GOOGLE') NOT NULL DEFAULT 'LOCAL',
                      provider_id      VARCHAR(255),
                      FOREIGN KEY (district_id) REFERENCES CITY_DISTRICT(district_id)
);
-- 參數
-- user 前三組密碼:  user1234
INSERT INTO USER
(email, district_id, user_address, email_verified, password, user_created_at,
 user_name, user_nickname, birthday, user_phone_num, user_status,
 monthly_spending, auth_provider, provider_id)
VALUES
    ('user1@gmail.com',  2, '桃園區提八路1號',      TRUE, '$2a$12$R28D55f8Bz2L8YLmtbB1hOB1VkNxn0EO2FXUY7fjJChv63nOGTmrK', '2026-07-03 18:50:11', 'TestUser',     'user', '1990-01-01', '0911111111', 'ACTIVE',  8000,  'LOCAL',  NULL),
    ('user2@gmail.com',  2, '桃園區中正路50號',          TRUE, '$2a$12$MAlxMsR3oBmdSghnfkXe..Iwez/iTE/mXoaSbseEfk7eiUlkwoC0a', '2024-02-03 14:05:47', '林淑惠',     '惠惠', '1988-07-22', '0922333444', 'WARNED',  2150, 'LOCAL',  NULL),
    ('user3@gmail.com',   3, '信義區松高路11號',          TRUE, '$2a$12$sXSgD5eKH9/J91J5of/5q.58Ae5FRVVYhK682FR1qvdqFUqFOeVKi', '2024-06-01 13:22:09', 'Alex Tsai', '小蔡', '1993-04-08', '0977888999', 'ACTIVE',  4200, 'LOCAL',  NULL),
    ('user4@gmail.com',   4, '臺中市西屯區臺灣大道300號',  TRUE, NULL, '2024-06-14 10:11:44', 'Jessica Wu', 'Jess', '1996-12-14', '0911222333', 'ACTIVE',  760,  'GOOGLE', '113985620174639205841'),
    ('user5@gmail.com',   5, '高雄市鳳山區青年路200號',    TRUE, NULL, '2024-07-08 16:38:27', 'Kevin Lin',  NULL,   '1990-08-19', '0922888777', 'ACTIVE',  0,    'GOOGLE', '102758493016283749561');


-- 1. 會員系統 - 消費級距
CREATE TABLE SPENDING_TIER (
                               tier_id     INT         PRIMARY KEY,
                               tier_name   VARCHAR(20),
                               min_amount  INT         NOT NULL,
                               max_amount  INT,
                               description VARCHAR(100)
);
-- 參數
INSERT INTO SPENDING_TIER (tier_id, tier_name, min_amount, max_amount, description)
VALUES
    (1, '一般會員', 0,    0,    '尚無消費紀錄'),
    (2, '銅級會員', 1,    1000, '每月消費 $1 – $1,000'),
    (3, '銀級會員', 1001, 3000, '每月消費 $1,001 – $3,000'),
    (4, '金級會員', 3001, NULL, '每月消費 $3,001 以上');


-- 2. 管理員 - 功能權限
CREATE TABLE ADMIN_ROLE (
                            permission_id   INT         AUTO_INCREMENT PRIMARY KEY,
                            permission_name VARCHAR(50),
                            permission_code VARCHAR(30) NOT NULL UNIQUE,
                            description     VARCHAR(100)
);
-- 參數 (種子資料)
INSERT INTO ADMIN_ROLE (permission_name, permission_code, description)
VALUES
    ('最新消息管理',       'NEWS',        '消息管理、發布消息通知'),
    ('專欄部落格內容管理', 'BLOG',        '文章檢舉處理、留言審核'),
    ('會員管理',           'MEMBER',      '消費者會員資料管理、權限控管'),
    ('小農管理',           'FARMER',      '小農案件審核、資料管理、權限控管'),
    ('管理員管理',         'ADMIN',       '管理員管理、權限控管'),
    ('商城管理',           'SHOP',        '管理優惠券、資料統計、熱門商品數據管理'),
    ('團購管理',           'GROUP_BUY',   '團購管理處理'),
    ('體驗活動管理',       'EVENT',       '活動審核、瀏覽、搜尋、狀態管理'),
    ('市場數據管理',       'MARKET_DATA', '數據更新'),
    ('網頁流量分析',       'ANALYTICS',   '用戶增加趨勢、訂單數量統計');

-- 2. 管理員 - 管理員帳號
CREATE TABLE ADMIN (
                       admin_id       INT          AUTO_INCREMENT PRIMARY KEY,
                       admin_email    VARCHAR(100) NOT NULL UNIQUE,
                       admin_password VARCHAR(255),
                       admin_name     VARCHAR(50),
                       admin_status   ENUM('ACTIVE', 'SUSPENDED', 'DELETED'),
                       created_at     DATETIME     NOT NULL,
                       updated_at     DATETIME
);
-- 參數
-- admin 密碼都是: admin1234
INSERT INTO ADMIN (admin_email, admin_password, admin_name, admin_status, created_at, updated_at)
VALUES
('admin01@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '王管理', 'ACTIVE',    '2024-01-01 09:00:00', '2024-06-15 10:30:00'),
('admin02@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '林管理', 'ACTIVE',    '2024-01-02 09:00:00', '2024-07-20 14:00:00'),
('admin03@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '陳管理', 'ACTIVE',    '2024-02-10 09:00:00', NULL),
('admin04@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '張管理', 'SUSPENDED', '2024-03-05 09:00:00', '2024-09-01 08:00:00'),
('admin05@farm.com', '$2a$10$dSXb.5E1CktD650efBJsR.PGeVZ7C2Un5YNrx8kQEzn.maBi7nNmm', '黃管理', 'DELETED',   '2024-04-20 09:00:00', '2025-01-10 17:00:00');

-- 3-1. 農場商品 - 商品大類表
CREATE TABLE MAINCATEGORY (
                              product_main_cat_id INT AUTO_INCREMENT PRIMARY KEY,
                              product_main_cat_name VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO MAINCATEGORY (product_main_cat_id, product_main_cat_name) VALUES
    (1, '水果'),
    (2, '葉菜類'),
    (3, '根莖類'),
    (4, '瓜果茄豆'),
    (5, '穀物雜糧'),
    (6, '豆類堅果'),
    (7, '菇蕈類'),
    (8, '香草辛香料'),
    (9, '茶葉咖啡'),
    (10, '蛋品'),
    (11, '蜂蜜甜品'),
    (12, '農產加工品');


-- 3-6. 農場商品 - 優惠卷
CREATE TABLE COUPON (
                        coupon_id VARCHAR(50) PRIMARY KEY,
                        coupon_info VARCHAR(255) NOT NULL,
                        issue_start_date DATETIME,
                        issue_end_date DATETIME,
                        amount INT,
                        min_spending INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO COUPON (coupon_id, coupon_info, issue_start_date, issue_end_date, amount, min_spending)
VALUES
('WELCOME100', '新會員首購滿500折100元', '2026-01-01 00:00:00', '2026-12-31 23:59:59', 100, 500);


-- 6. 專欄部落格 - 部落格類別
CREATE TABLE BLOG_TYPE (
                           blog_type_id INT AUTO_INCREMENT PRIMARY KEY,
                           blog_type_name VARCHAR(15),
                           blog_type_img LONGBLOB,
                           blog_type_text VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_TYPE (blog_type_id, blog_type_name, blog_type_img, blog_type_text)
VALUES
(1, '產地日記', NULL, '分享農友日常、作物成長與產地故事'),
(2, '蔬果知識分享', NULL, '蔬果挑選、保存、營養小百科'),
(3, '農作體驗回顧', NULL, '參加體驗活動或產地參訪的心得'),
(4, '食譜分享', NULL, '在家就能做的料理');


-- ==========================================
-- 一、會員與小農表 (依賴地址)
-- ==========================================


-- 1-2 小農會員（先建；已移除 review_id）
-- ====================================================================
CREATE TABLE FARMER (
                        farmer_id         INT           AUTO_INCREMENT PRIMARY KEY,
                        email             VARCHAR(255)  UNIQUE,
                        email_verified    BOOLEAN,
                        district_id       INT,
                        uploaded_at       DATETIME,
                        password          VARCHAR(255),
                        farm_address      VARCHAR(100),
                        farm_name         VARCHAR(50),
                        loc_lat           DECIMAL(10,8),
                        loc_long          DECIMAL(11,8),
                        farm_desc         TEXT,
                        farmer_created_at DATETIME,
                        farmer_phone_num  VARCHAR(15),
                        farmer_status     ENUM('PENDING', 'ACTIVE', 'SUSPENDED') DEFAULT 'PENDING',
                        FOREIGN KEY (district_id) REFERENCES CITY_DISTRICT(district_id)
);

-- farmer_status 已依「最新審核結果」對應好：
--  farmer01 審核 APPROVED  -> ACTIVE
--  farmer02 最新 APPROVED  -> ACTIVE
--  farmer03 審核 REVIEWING -> PENDING（管理員審核中，但小農端只看到待審）
--  farmer04 管理員停權     -> SUSPENDED
--  farmer05 審核 PENDING   -> PENDING

-- 密碼都是: farmer1234
INSERT INTO FARMER (email, email_verified, district_id, uploaded_at, password, farm_address, farm_name, loc_lat, loc_long, farm_desc, farmer_created_at, farmer_phone_num, farmer_status)
VALUES
    ('farmer01@gmail.com', TRUE, 1, '2024-02-01 10:00:00', '$2a$12$nrUvcDLzS.AMY3hppatbuuaq0UwL9/2aXj8YS8oCNThL22oPpXVcm', '中正路500號', '陽光農場', 25.04776000, 121.53185000, '專注有機蔬菜栽培，堅持無農藥',   '2024-02-03 14:00:00', '0911111111', 'ACTIVE'),
    ('farmer02@gmail.com', TRUE, 2, '2024-03-10 09:00:00', '$2a$12$nrUvcDLzS.AMY3hppatbuuaq0UwL9/2aXj8YS8oCNThL22oPpXVcm', '大同街200號', '綠野農場', 25.06321000, 121.51234000, '自然農法種植，提供當季新鮮蔬果', '2024-03-22 15:00:00', '0922222222', 'ACTIVE'),
    ('farmer03@gmail.com', TRUE, 3, '2024-05-15 08:00:00', '$2a$12$nrUvcDLzS.AMY3hppatbuuaq0UwL9/2aXj8YS8oCNThL22oPpXVcm', '中山路333號', '山間農場', 24.98765000, 121.54321000, '山區有機農場，專售高山茶與蔬菜', '2024-05-15 08:00:00', '0933333333', 'PENDING'),
    ('farmer04@gmail.com', TRUE, 4, '2024-03-10 09:00:00', '$2a$12$nrUvcDLzS.AMY3hppatbuuaq0UwL9/2aXj8YS8oCNThL22oPpXVcm', '仁愛路88號',  '海風農場', 25.12345000, 121.73456000, '靠海農場，專售海鹽與特色農產品', '2024-03-10 09:00:00', '0944444444', 'SUSPENDED'),
    ('farmer05@gmail.com', TRUE, 5, '2024-06-01 13:00:00', '$2a$12$nrUvcDLzS.AMY3hppatbuuaq0UwL9/2aXj8YS8oCNThL22oPpXVcm', '板橋路77號',  '稻香農場', 24.87654000, 121.45678000, '傳統水稻種植，提供在地新鮮稻米', '2024-06-01 13:00:00', '0955555555', 'PENDING');


-- ====================================================================
-- 2-4 小農申請案件（後建；farmer_id 指向 FARMER，含重審暫存欄位）
-- ====================================================================
CREATE TABLE FARMER_REVIEW (
                               review_id             INT           AUTO_INCREMENT PRIMARY KEY,
                               farmer_id             INT,
                               admin_id              INT,
                               review_round          INT           NOT NULL DEFAULT 1,
                               review_status         ENUM('PENDING', 'REVIEWING', 'APPROVED', 'REJECTED') NOT NULL,  -- 管理員視角
                               submitted_at          DATETIME      NOT NULL,
                               reviewed_at           DATETIME,
                               reject_reason         VARCHAR(255),
                               notes                 TEXT,
                               cert_file_land        LONGBLOB,
                               cert_file_product     LONGBLOB,
                               cert_file_identity    LONGBLOB,
    -- 重審暫存欄位：小農申請變更的內容，審核通過後才寫回 FARMER --
                               submitted_farm_name    VARCHAR(50)   NULL,
                               submitted_farm_address VARCHAR(100)  NULL,
                               submitted_district_id  INT           NULL,
                               submitted_loc_lat      DECIMAL(10,8) NULL,
                               submitted_loc_long     DECIMAL(11,8) NULL,
                               FOREIGN KEY (farmer_id)            REFERENCES FARMER(farmer_id),
                               FOREIGN KEY (admin_id)             REFERENCES ADMIN(admin_id),
                               CONSTRAINT fk_review_submitted_district
                                   FOREIGN KEY (submitted_district_id) REFERENCES CITY_DISTRICT(district_id)
);

-- 第 2、3 筆同屬 farmer_id = 2，示範「一個 farmer 多筆審核」
-- proposed_* 僅在「申請變更農場資料」時填值；首次申請留 NULL
INSERT INTO FARMER_REVIEW
(farmer_id, admin_id, review_round, review_status, submitted_at, reviewed_at, reject_reason, notes,
 cert_file_land, cert_file_product, cert_file_identity,
 submitted_farm_name, submitted_farm_address, submitted_district_id, submitted_loc_lat, submitted_loc_long)
VALUES
    (1, 1, 1, 'APPROVED',  '2024-02-01 10:00:00', '2024-02-03 14:00:00', NULL,             '資料齊全，審核通過',
     NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL),
    (2, 2, 1, 'REJECTED',  '2024-03-10 09:00:00', '2024-03-12 11:00:00', '土地文件不清晰', '請重新上傳土地證明',
     NULL, NULL, NULL,
     '綠野有機農場', '大同街250號', 2, 25.06400000, 121.51300000),
    (2, 1, 2, 'APPROVED',  '2024-03-20 10:00:00', '2024-03-22 15:00:00', NULL,             '補件後審核通過，已更新農場資料',
     NULL, NULL, NULL,
     '綠野有機農場', '大同街250號', 2, 25.06400000, 121.51300000),
    (3, 3, 1, 'REVIEWING', '2024-05-15 08:00:00', NULL,                  NULL,             '農場遷址審核中',
     NULL, NULL, NULL,
     '山間農場',     '中山路888號', 4, 25.13000000, 121.74000000),
    (5, 3, 1, 'PENDING',   '2024-06-01 13:00:00', NULL,                  NULL,             NULL,
     NULL, NULL, NULL,
     NULL, NULL, NULL, NULL, NULL);

-- ==========================================
-- 二、依賴 User, Farmer, Admin 的進階表
-- ==========================================

-- 2. 管理員 - 管理功能對照
CREATE TABLE ADMIN_PERMISSION_ROLE (
                                       admin_id      INT,
                                       permission_id INT,
                                       PRIMARY KEY (admin_id, permission_id),
                                       FOREIGN KEY (admin_id)      REFERENCES ADMIN(admin_id),
                                       FOREIGN KEY (permission_id) REFERENCES ADMIN_ROLE(permission_id)
);
-- 參數
INSERT INTO ADMIN_PERMISSION_ROLE (admin_id, permission_id)
VALUES
-- admin1（王管理）：全權限
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
-- admin2（林管理）：NEWS, BLOG, MEMBER —— 故意不給 FARMER，當「對比帳號」
(2, 1), (2, 2), (2, 3),
-- admin3（陳管理）：MEMBER, FARMER, SHOP, GROUP_BUY
(3, 3), (3, 4), (3, 6), (3, 7),
-- admin4（張管理，已停權）：EVENT, MARKET_DATA
(4, 8), (4, 9),
-- admin5（黃管理，已刪除）：ANALYTICS
(5, 10);


-- 2. 管理員 - 最新消息
CREATE TABLE NEWS (
                      news_id      INT          AUTO_INCREMENT PRIMARY KEY,
                      admin_id     INT          NOT NULL,
                      title        VARCHAR(50),
                      content      VARCHAR(500),
                      cover_image  LONGBLOB,
                      publish_time DATETIME,
                      news_status  ENUM('VISIBLE', 'HIDDEN', 'DRAFT'),
                      created_at   DATETIME,
                      FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
);
-- 參數
INSERT INTO NEWS (admin_id, title, content, cover_image, publish_time, news_status, created_at)
VALUES
    (1, '平台正式上線公告',     '你儂我農平台正式上線，歡迎小農與消費者加入我們的大家庭！',                       NULL, '2024-01-15 09:00:00', 'VISIBLE', '2024-01-14 18:00:00'),
    (2, '春季蔬果產季開始',     '春季蔬果陸續上架，歡迎選購當季新鮮農產品。',                                   NULL, '2024-03-01 09:00:00', 'VISIBLE', '2024-02-28 17:00:00'),
    (1, '系統維護通知',         '平台將於 2024/04/10 凌晨 2:00 – 4:00 進行系統維護，期間暫停服務。',           NULL, '2024-04-08 12:00:00', 'VISIBLE', '2024-04-08 10:00:00'),
    (3, '小農申請流程更新說明', '即日起小農申請流程新增第二階段審核，請備妥相關證明文件再行送件。',               NULL, '2024-05-20 10:00:00', 'HIDDEN',  '2024-05-19 16:00:00'),
    (2, '端午節限定農產品上架', '多款端午節限定農產品即將上架，敬請期待！',                                       NULL, '2024-06-05 09:00:00', 'VISIBLE', '2024-06-04 15:00:00');


-- 3-7. 農場商品 - 優惠卷明細
CREATE TABLE COUPON_DETAILS (
                                user_id INT,
                                coupon_id VARCHAR(50),
                                status ENUM('UNUSED', 'USED', 'EXPIRED'),

                                PRIMARY KEY (user_id, coupon_id),
                                FOREIGN KEY (user_id) REFERENCES USER(user_id),
                                FOREIGN KEY (coupon_id) REFERENCES COUPON(coupon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO COUPON_DETAILS (user_id, coupon_id, status) VALUES
    (1, 'WELCOME100', 'UNUSED');


-- 3-2. 農場商品 - 商品子類表
CREATE TABLE SUBCATEGORY (
                             sub_cat_class_id INT AUTO_INCREMENT PRIMARY KEY,
                             product_main_cat_id INT NOT NULL,
                             sub_cat_class_name VARCHAR(50) NOT NULL,
                             FOREIGN KEY (product_main_cat_id) REFERENCES MAINCATEGORY(product_main_cat_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO SUBCATEGORY (sub_cat_class_id, product_main_cat_id, sub_cat_class_name) VALUES
-- 1 水果
(1, 1, '香蕉'), (2, 1, '鳳梨'), (3, 1, '芒果'), (4, 1, '芭樂'), (5, 1, '蓮霧'),
(6, 1, '木瓜'), (7, 1, '柑橘類'), (8, 1, '葡萄'), (9, 1, '草莓'), (10, 1, '西瓜'),
(11, 1, '哈密瓜'), (12, 1, '梨'), (13, 1, '桃李'), (14, 1, '荔枝龍眼'), (15, 1, '釋迦'),
(16, 1, '火龍果'), (17, 1, '酪梨'),
-- 2 葉菜類
(18, 2, '高麗菜'), (19, 2, '大白菜'), (20, 2, '小白菜'), (21, 2, '青江菜'), (22, 2, '菠菜'),
(23, 2, '空心菜'), (24, 2, '地瓜葉'), (25, 2, '萵苣生菜'), (26, 2, '芥藍'), (27, 2, '韭菜'),
(28, 2, '蔥'),
-- 3 根莖類
(29, 3, '蘿蔔'), (30, 3, '胡蘿蔔'), (31, 3, '馬鈴薯'), (32, 3, '地瓜'), (33, 3, '芋頭'),
(34, 3, '山藥'), (35, 3, '洋蔥'), (36, 3, '蒜頭'), (37, 3, '薑'), (38, 3, '竹筍'),
(39, 3, '蓮藕'),
-- 4 瓜果茄豆
(40, 4, '番茄'), (41, 4, '小黃瓜'), (42, 4, '絲瓜'), (43, 4, '苦瓜'), (44, 4, '南瓜'),
(45, 4, '櫛瓜'), (46, 4, '茄子'), (47, 4, '甜椒'), (48, 4, '辣椒'), (49, 4, '玉米'),
(50, 4, '四季豆'), (51, 4, '秋葵'),
-- 5 穀物雜糧
(52, 5, '白米'), (53, 5, '糙米'), (54, 5, '黑米紫米'), (55, 5, '小米'), (56, 5, '燕麥'),
(57, 5, '麵粉'), (58, 5, '蕎麥'), (59, 5, '薏仁'), (60, 5, '藜麥'),
-- 6 豆類堅果
(61, 6, '黃豆'), (62, 6, '黑豆'), (63, 6, '紅豆'), (64, 6, '綠豆'), (65, 6, '花生'),
(66, 6, '芝麻'), (67, 6, '核桃'), (68, 6, '腰果'),
-- 7 菇蕈類
(69, 7, '香菇'), (70, 7, '金針菇'), (71, 7, '杏鮑菇'), (72, 7, '鴻喜菇'), (73, 7, '木耳'),
(74, 7, '洋菇'),
-- 8 香草辛香料
(75, 8, '九層塔'), (76, 8, '香菜'), (77, 8, '薄荷'), (78, 8, '迷迭香'), (79, 8, '香茅'),
(80, 8, '薑黃'), (81, 8, '胡椒'),
-- 9 茶葉咖啡
(82, 9, '綠茶'), (83, 9, '烏龍茶'), (84, 9, '紅茶'), (85, 9, '高山茶'), (86, 9, '咖啡豆'),
(87, 9, '花草茶'),
-- 10 蛋品
(88, 10, '雞蛋'), (89, 10, '鴨蛋'), (90, 10, '鵪鶉蛋'), (91, 10, '皮蛋鹹蛋'),
-- 11 蜂蜜甜品
(92, 11, '蜂蜜'), (93, 11, '蜂王乳'), (94, 11, '黑糖'), (95, 11, '麥芽糖'),
-- 12 農產加工品
(96, 12, '果乾蜜餞'), (97, 12, '果醬'), (98, 12, '醬菜泡菜'), (99, 12, '豆製品'),
(100, 12, '米麵製品'), (101, 12, '醬料'), (102, 12, '酒醋釀造');


-- 3-3. 農場商品 - 商城農場產品
CREATE TABLE PRODUCT_DETAIL (
                                product_id INT AUTO_INCREMENT PRIMARY KEY,
                                sub_cat_class_id INT NOT NULL,
                                farmer_id INT NOT NULL,
                                retail_price INT NOT NULL,
                                group_price INT,
                                unit_pricing_measure VARCHAR(20) NOT NULL,
                                product_image LONGBLOB,
                                is_group_buy BOOLEAN,
                                description VARCHAR(500),
                                status ENUM('ACTIVE', 'INACTIVE'),
                                product_name varchar(20),
                                FOREIGN KEY (sub_cat_class_id) REFERENCES SUBCATEGORY(sub_cat_class_id),
                                FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO PRODUCT_DETAIL (product_id, sub_cat_class_id, farmer_id, retail_price, group_price, unit_pricing_measure, product_image, is_group_buy, description, status,product_name)
VALUES
(1, 1, 1, 120, 100, '包/300g', NULL, 1, '陽光農場有機香蕉，堅持無農藥栽培，果肉綿密。',     'ACTIVE','陽光農場有機香蕉'),
 (2, 1, 2, 180, 150, '盒/600g', NULL, 1, '綠野農場自然農法香蕉，當季採收，香氣清甜。',       'ACTIVE','綠野農場香蕉'),
 (3, 1, 3, 250, 220, '箱/3kg',  NULL, 1, '山間農場山區栽培，日夜溫差大，香氣特別濃郁。',     'ACTIVE','山間農場香蕉'),
 (4, 1, 4, 160, 140, '包/500g', NULL, 1, '海風農場臨海栽培，鬆甜綿密，適合蒸烤。',           'ACTIVE','海風農場香蕉'),
 (5, 1, 5, 199, 169, '箱/6入',  NULL, 1, '稻香農場香蕉水果箱，六入家庭號，產地直送。',       'ACTIVE','稻香農場香蕉箱'),
 -- 水果類
 (6,  3,  2, 320, 280, '箱/5kg',   NULL, 1, '台南玉井愛文芒果，果肉細緻香甜，冷藏配送。',   'ACTIVE',   '玉井愛文芒果'),
 (7,  2,  3, 280, 240, '顆/1.5kg', NULL, 1, '關廟金鑽鳳梨，酸甜適中不咬舌。',               'ACTIVE',   '關廟金鑽鳳梨'),
 (8,  9,  1, 450, 400, '盒/400g',  NULL, 1, '大湖高架草莓，低農藥檢驗合格，當日採收。',     'ACTIVE',   '大湖高架草莓'),
 (9,  8,  2, 360, 320, '箱/2kg',   NULL, 1, '巨峰葡萄產季限定，果粉自然，甜度高。',         'ACTIVE',   '巨峰葡萄'),
 (10, 7,  3, 150, 130, '袋/2kg',   NULL, 0, '茂谷柑皮薄多汁，香氣清爽。',                   'ACTIVE',   '茂谷柑'),
-- 葉菜類
 (11, 18, 1,  80,  65, '顆/1.2kg', NULL, 0, '高山高麗菜，清脆回甘，生食熱炒皆宜。',         'ACTIVE',   '梨山高麗菜'),
 (12, 24, 1,  45,  35, '把/300g',  NULL, 0, '有機地瓜葉，當日採收，無農藥。',               'ACTIVE',   '有機地瓜葉'),
 (13, 22, 2,  55,  45, '把/250g',  NULL, 0, '自然農法菠菜，無農藥殘留。',                   'ACTIVE',   '自然農法菠菜'),
 (14, 28, 4,  90,  75, '把/200g',  NULL, 0, '三星蔥蔥白長、辛香濃郁。',                     'ACTIVE',   '三星青蔥'),
-- 根莖類
 (15, 32, 5, 120, 100, '箱/3kg',   NULL, 1, '台農57號地瓜，鬆軟香甜，適合烤food。',         'ACTIVE',   '台農57號地瓜'),
 (16, 33, 5, 200, 170, '袋/2kg',   NULL, 1, '大甲芋頭鬆綿químico，芋香濃厚。',              'ACTIVE',   '大甲芋頭'),
 (17, 30, 1,  60,  50, '袋/1kg',   NULL, 0, '有機胡蘿蔔，甜度高，適合打汁。',               'ACTIVE',   '有機胡蘿蔔'),
-- 瓜果茄豆
 (18, 40, 2, 140, 120, '盒/600g',  NULL, 1, '溫室玉女小番茄，皮薄多汁。',                   'ACTIVE',   '玉女小番茄'),
 (19, 49, 5, 100,  85, '箱/6入',   NULL, 1, '水果玉米生食也甘甜，纖維細。',                 'ACTIVE',   '水果玉米'),
 (20, 44, 4, 110,  95, '顆/2kg',   NULL, 0, '栗子南瓜粉質綿密，適合濃湯。',                 'ACTIVE',   '栗子南瓜'),
-- 穀物雜糧
 (21, 52, 5, 320, 280, '包/2kg',   NULL, 1, '池上米，粒粒飽滿，冷飯也好吃。',               'ACTIVE',   '池上米'),
 (22, 53, 5, 280, 250, '包/2kg',   NULL, 1, '有機糙米，保留胚芽，營養完整。',               'ACTIVE',   '有機糙米'),
 (23, 65, 4, 220, 190, '包/600g',  NULL, 0, '北港花生，低溫烘焙，無添加。',                 'ACTIVE',   '北港花生'),
-- 菇蕈類
 (24, 69, 3, 260, 230, '包/300g',  NULL, 1, '埔里段木香菇，菇傘厚實，香氣足。',             'ACTIVE',   '埔里段木香菇'),
 (25, 71, 3,  90,  75, '盒/250g',  NULL, 0, '杏鮑菇肉質厚實，口感彈牙。',                   'ACTIVE',   '鮮採杏鮑菇'),
-- 茶葉咖啡
 (26, 85, 3, 850, 780, '罐/150g',  NULL, 1, '梨山高山烏龍，手採春茶，喉韻回甘。',           'ACTIVE',   '梨山高山茶'),
 (27, 86, 3, 600, 550, '包/227g',  NULL, 1, '古坑咖啡豆，中焙，帶堅果與可可風味。',         'ACTIVE',   '古坑咖啡豆'),
-- 蛋品
 (28, 88, 1, 130, 110, '盒/10入',  NULL, 0, '放牧雞蛋，蛋黃飽滿，日日新鮮。',               'ACTIVE',   '放牧土雞蛋'),
-- 蜂蜜 / 加工品（測試 INACTIVE 用）
 (29, 92, 4, 480, 430, '瓶/700g',  NULL, 1, '龍眼蜜，天然熟成，未加糖。',                   'INACTIVE', '龍眼蜂蜜'),
 (30, 97, 2, 180, 160, '罐/250g',  NULL, 0, '手工草莓果醬，無添加香料與色素。',             'INACTIVE', '手工草莓果醬');




-- 3-4. 農場商品 - 產品圖片
CREATE TABLE PRODUCT_IMAGE (
                               product_image_id INT AUTO_INCREMENT PRIMARY KEY,
                               product_id INT NOT NULL,
                               product_image LONGBLOB,
                               FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO PRODUCT_IMAGE (product_image_id, product_id, product_image) VALUES
   (1, 1, NULL), (2,  1,  NULL), (3,  1,  NULL),                 -- 陽光農場有機香蕉 (共3張)
 (4,  2,  NULL), (5,  2,  NULL),                 -- 綠野農場香蕉 (2張)
 (6,  3,  NULL), (7,  3,  NULL), (8,  3,  NULL), -- 山間農場香蕉 (3張)
 (9,  4,  NULL),                                 -- 海風農場香蕉 (1張)
 (10, 5,  NULL), (11, 5,  NULL),                 -- 稻香農場香蕉箱 (2張)
 (12, 6,  NULL), (13, 6,  NULL), (14, 6,  NULL), -- 玉井愛文芒果 (3張)
 (15, 7,  NULL), (16, 7,  NULL),                 -- 關廟金鑽鳳梨 (2張)
 (17, 8,  NULL), (18, 8,  NULL), (19, 8,  NULL), -- 大湖高架草莓 (3張)
 (20, 9,  NULL), (21, 9,  NULL),                 -- 巨峰葡萄 (2張)
 (22, 10, NULL),                                 -- 茂谷柑 (1張)
 (23, 11, NULL), (24, 11, NULL),                 -- 梨山高麗菜 (2張)
 (25, 12, NULL),                                 -- 有機地瓜葉 (1張)
 (26, 13, NULL), (27, 13, NULL),                 -- 自然農法菠菜 (2張)
 (28, 14, NULL),                                 -- 三星青蔥 (1張)
 (29, 15, NULL), (30, 15, NULL), (31, 15, NULL), -- 台農57號地瓜 (3張)
 (32, 16, NULL), (33, 16, NULL),                 -- 大甲芋頭 (2張)
 (34, 17, NULL),                                 -- 有機胡蘿蔔 (1張)
 (35, 18, NULL), (36, 18, NULL), (37, 18, NULL), -- 玉女小番茄 (3張)
 (38, 19, NULL), (39, 19, NULL),                 -- 水果玉米 (2張)
 (40, 20, NULL),                                 -- 栗子南瓜 (1張)
 (41, 21, NULL), (42, 21, NULL), (43, 21, NULL), -- 池上米 (3張)
 (44, 22, NULL), (45, 22, NULL),                 -- 有機糙米 (2張)
 (46, 23, NULL),                                 -- 北港花生 (1張)
 (47, 24, NULL), (48, 24, NULL), (49, 24, NULL), -- 埔里段木香菇 (3張)
 (50, 25, NULL), (51, 25, NULL),                 -- 鮮採杏鮑菇 (2張)
 (52, 26, NULL), (53, 26, NULL), (54, 26, NULL), -- 梨山高山茶 (3張)
 (55, 27, NULL), (56, 27, NULL),                 -- 古坑咖啡豆 (2張)
 (57, 28, NULL),                                 -- 放牧土雞蛋 (1張)
 (58, 29, NULL), (59, 29, NULL),                 -- 龍眼蜂蜜 (2張)
 (60, 30, NULL);                                 -- 手工草莓果醬 (1張)


-- 3-5. 農場商品 - 一般會員商品收藏列表
CREATE TABLE GENERAL_MEMBER_PRODUCT_WISHLIST (
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (product_id, user_id),
    FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO GENERAL_MEMBER_PRODUCT_WISHLIST (product_id, user_id) VALUES
(1, 1);


-- 3-8. 農場商品 - 產品購物車
CREATE TABLE PRODUCT_SHOPPING_CART (
    product_id INT NOT NULL,
    user_id INT NOT NULL,
    quantity INT NOT NULL,
    PRIMARY KEY (product_id, user_id),
    FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO PRODUCT_SHOPPING_CART (product_id, user_id, quantity) VALUES
(1, 1, 2);


-- 3-9. 農場商品 - 訂單 (將 ORDER 改為 ORDERS 避開保留字)
DROP TABLE IF EXISTS order_item;
DROP TABLE IF EXISTS orders;
CREATE TABLE ORDERS (
    order_id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    recipient_name VARCHAR(50) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    coupon_id VARCHAR(50),
	shipping_address VARCHAR(100) NOT NULL,
    payment_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount INT NOT NULL,
    discount_amount INT NOT NULL DEFAULT 0,
    shipping_fee INT NOT NULL DEFAULT 0,
    final_payment INT NOT NULL,
    order_status ENUM('pending', 'completed') DEFAULT 'pending',
    completed_at DATETIME,
    CONSTRAINT PK_ORDERS_OID PRIMARY KEY (order_id),
    CONSTRAINT FK_ORDERS_UID FOREIGN KEY (user_id) REFERENCES user (user_id),
    CONSTRAINT FK_ORDERS_COUPID FOREIGN KEY (coupon_id) REFERENCES coupon (coupon_id)
)
AUTO_INCREMENT = 3001
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4; 

-- 參數
INSERT INTO ORDERS (user_id, recipient_name, recipient_phone, shipping_address, payment_id, created_at, total_amount, discount_amount, final_payment, order_status, completed_at)
VALUES (3, '小蔡', '0912345678', '臺北市中正區重慶南路一段122號', 8888, '2026-03-01 14:05:00', 1000, 0, 1000, 'completed', '2026-03-11 15:30:00');
INSERT INTO ORDERS (user_id, recipient_name, recipient_phone, shipping_address, payment_id, total_amount, discount_amount, final_payment)
VALUES (3, '小蔡', '0912345678', '臺北市中正區重慶南路一段122號', 8881, 880, 0, 880);


-- 3-10. 農場商品 - 訂單明細
DROP TABLE IF EXISTS order_item;
CREATE TABLE ORDER_ITEM (
    order_item_id INT NOT NULL AUTO_INCREMENT,
	order_id INT NOT NULL,
    product_id INT NOT NULL,
    product_name VARCHAR(50) NOT NULL,
    farmer_id INT NOT NULL,
    quantity INT NOT NULL,
    price INT NOT NULL,
    shipped_status ENUM('pending', 'shipping', 'delivered') DEFAULT 'pending',
    shipped_at DATETIME,
    received_at DATETIME,
	payout_status VARCHAR(50) DEFAULT 'pending',
    CONSTRAINT PK_ORDITEM_ORDITEMID PRIMARY KEY (order_item_id),
	CONSTRAINT FK_ORDITEM_OID FOREIGN KEY (order_id) REFERENCES orders (order_id),
    CONSTRAINT FK_ORDITEM_PRDID FOREIGN KEY (product_id) REFERENCES product_detail (product_id),
    CONSTRAINT FK_ORDITEM_FID FOREIGN KEY (farmer_id) REFERENCES farmer (farmer_id)
)
AUTO_INCREMENT = 4001
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 參數
INSERT INTO ORDER_ITEM (order_id, product_id, product_name, farmer_id, quantity, price, shipped_status, shipped_at, received_at, payout_status)
VALUES (3001, 3, '屏東香蕉', 1, 4, 250, 'delivered', '2026-03-02 10:00:00', '2026-03-03 15:30:00', 'paid');
INSERT INTO ORDER_ITEM (order_id, product_id, product_name, farmer_id, quantity, price, shipped_status, shipped_at, received_at, payout_status)
VALUES (3002, 3, '屏東香蕉', 1, 3, 250, 'delivered', '2026-04-02 10:00:00', '2026-04-03 15:30:00', 'paid');


-- 4. 團購 - 團購活動表
CREATE TABLE GROUP_BUY (
    group_buy_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT,
    host_user_id INT,
    target_amount INT NOT NULL,
    group_price INT,
    open_datetime DATETIME,
    ddl_datetime DATETIME,
    status ENUM('open','success','failed','cancelled','pending') NOT NULL DEFAULT 'pending',
    created_at DATETIME,
    request_status ENUM('pending','approved','rejected') NOT NULL DEFAULT 'pending',
    request_datetime DATETIME,
    reply_datetime DATETIME,
    reject_reason VARCHAR(255),
    pickup_address VARCHAR(255),
    FOREIGN KEY (product_id) REFERENCES PRODUCT_DETAIL(product_id),
    FOREIGN KEY (host_user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
-- 資料規則（對應後端 GroupBuyService 的邏輯，改資料時請一併遵守）：
--  1. group_price 必須 = PRODUCT_DETAIL.group_price（hostRequest() 是直接抄商品價格，不會讓兩者不同）
--  2. target_amount 必須 >= group_price * 2（hostRequest() 的最低達標金額檢查）
--  3. 只有 status='success' 的團購才可以有 GB_ORDER
--  4. 每筆有參與紀錄的團購，都必須有一筆 is_host=1 且該 user_id = host_user_id
--     （團購主沒有參與紀錄的話，mySuccessOrders 查不到他的訂單 → 他永遠無法確認收貨）
--  5. status='open' 的 ddl_datetime 必須是未來時間，否則排程 checkExpiredGroupBuys() 會把它結算掉
--  6. 團購只用 ACTIVE 小農（1、2）的 ACTIVE 且 is_group_buy=1 商品：1(100)、2(150)、6(280)、8(400)、9(320)、18(120)
--     團購主只用 ACTIVE 且有密碼的本地會員（user 1、3）
--  ※ 1~4 保留原本的邊界情境（停權/待審小農、WARNED 會員、GOOGLE 帳號），僅修正對不上的數值
INSERT INTO GROUP_BUY (group_buy_id,product_id,host_user_id,target_amount,group_price,open_datetime,ddl_datetime,`status`,created_at,request_status,request_datetime,reply_datetime,reject_reason,pickup_address
) VALUES
-- ===== 邊界情境（沿用原本 1~4 的用途；group_price 已對齊商品）=====
-- 1: 已成團，對應訂單 90001（notification 種子有引用「團購1／訂單90001」，故維持 success）
(1, 1, 1, 3000, 100, '2026-03-05 00:00:00', '2026-03-12 23:59:59', 'success', '2026-03-04 18:00:00', 'approved', '2026-03-04 10:00:00', '2026-03-04 11:00:00', NULL,'桃園市中壢區復興路46號9樓'),
-- 2: 停權小農(4)的商品 + GOOGLE 帳號團購主(5)：待審核情境
(2, 4, 5, 5000, 140, '2026-03-02 00:00:00', '2026-03-20 23:59:59', 'pending', '2026-03-10 09:00:00', 'pending', '2026-03-10 09:00:00', NULL, NULL,'桃園市蘆竹區南崁路一段10號'),
-- 3: 待審小農(5)的商品 + WARNED 會員(2)：已拒絕情境
(3, 5, 2, 2000, 169,'2026-03-04 00:00:00', '2026-03-18 23:59:59', 'cancelled', '2026-03-09 14:30:00', 'rejected', '2026-03-09 14:30:00', '2026-03-09 16:00:00', '商品庫存不足，無法開團','台北市中山區中正路66號'),
-- 4: 待審小農(3)的商品：待審核情境
(4, 3, 3, 3000, 220, '2026-03-05 00:00:00', '2026-03-12 23:59:59', 'pending', '2026-03-02 18:00:00', 'pending', '2026-03-04 10:00:00', '2026-03-04 11:00:00', NULL,'我是地址'),

-- ===== 開團中 12 筆（ddl 皆為未來；公開頁一頁 10 筆 → 可測到第 2 頁）=====
-- 5: ddl 只剩 2 天 → 測「⏰ 即將截止」標籤
(5,  1,  1, 3000, 100, '2026-07-10 00:00:00', '2026-07-19 23:59:59', 'open', '2026-07-09 10:00:00', 'approved', '2026-07-08 09:00:00', '2026-07-09 10:00:00', NULL, '桃園市中壢區復興路46號9樓'),
(6,  2,  3, 3000, 150, '2026-07-10 00:00:00', '2026-08-05 23:59:59', 'open', '2026-07-09 11:00:00', 'approved', '2026-07-08 09:30:00', '2026-07-09 11:00:00', NULL, '臺北市信義區松高路11號'),
(7,  6,  1, 3000, 280, '2026-07-11 00:00:00', '2026-08-07 23:59:59', 'open', '2026-07-10 14:00:00', 'approved', '2026-07-09 10:00:00', '2026-07-10 14:00:00', NULL, '桃園市中壢區復興路46號9樓'),
(8,  8,  3, 4000, 400, '2026-07-11 00:00:00', '2026-08-09 23:59:59', 'open', '2026-07-10 09:00:00', 'approved', '2026-07-09 08:30:00', '2026-07-10 09:00:00', NULL, '臺北市信義區松高路11號'),
(9,  9,  1, 3200, 320, '2026-07-12 00:00:00', '2026-08-11 23:59:59', 'open', '2026-07-11 15:00:00', 'approved', '2026-07-10 13:00:00', '2026-07-11 15:00:00', NULL, '桃園市中壢區復興路46號9樓'),
(10, 18, 3, 2400, 120, '2026-07-12 00:00:00', '2026-08-13 23:59:59', 'open', '2026-07-11 10:30:00', 'approved', '2026-07-10 09:00:00', '2026-07-11 10:30:00', NULL, '臺北市信義區松高路11號'),
(11, 1,  3, 2000, 100, '2026-07-13 00:00:00', '2026-08-15 23:59:59', 'open', '2026-07-12 16:00:00', 'approved', '2026-07-11 11:00:00', '2026-07-12 16:00:00', NULL, '臺北市信義區松高路11號'),
(12, 2,  1, 3000, 150, '2026-07-13 00:00:00', '2026-08-17 23:59:59', 'open', '2026-07-12 13:00:00', 'approved', '2026-07-11 10:00:00', '2026-07-12 13:00:00', NULL, '桃園市中壢區復興路46號9樓'),
(13, 6,  3, 5600, 280, '2026-07-14 00:00:00', '2026-08-19 23:59:59', 'open', '2026-07-13 09:30:00', 'approved', '2026-07-12 08:00:00', '2026-07-13 09:30:00', NULL, '臺北市信義區松高路11號'),
(14, 8,  1, 4000, 400, '2026-07-14 00:00:00', '2026-08-21 23:59:59', 'open', '2026-07-13 14:30:00', 'approved', '2026-07-12 11:00:00', '2026-07-13 14:30:00', NULL, '桃園市中壢區復興路46號9樓'),
(15, 9,  3, 3200, 320, '2026-07-15 00:00:00', '2026-08-23 23:59:59', 'open', '2026-07-14 10:00:00', 'approved', '2026-07-13 09:00:00', '2026-07-14 10:00:00', NULL, '臺北市信義區松高路11號'),
-- 16: 已達標但還沒到截止 → 測「已達標，等待截止結算」
(16, 18, 1, 1200, 120, '2026-07-15 00:00:00', '2026-08-25 23:59:59', 'open', '2026-07-14 11:00:00', 'approved', '2026-07-13 10:00:00', '2026-07-14 11:00:00', NULL, '桃園市中壢區復興路46號9樓'),

-- ===== 已成團 4 筆（+ 上面的 1，共 5 筆有訂單）=====
(17, 2,  3, 2700, 150, '2026-05-01 00:00:00', '2026-05-10 23:59:59', 'success', '2026-04-30 11:00:00', 'approved', '2026-04-29 09:30:00', '2026-04-30 11:00:00', NULL, '臺北市信義區松高路11號'),
(18, 6,  1, 3000, 280, '2026-05-15 00:00:00', '2026-05-24 23:59:59', 'success', '2026-05-14 14:00:00', 'approved', '2026-05-13 10:00:00', '2026-05-14 14:00:00', NULL, '桃園市中壢區復興路46號9樓'),
-- 19: 訂單 90004 已送達待確認 → 給 user3 測「確認收貨」
(19, 8,  3, 3600, 400, '2026-06-01 00:00:00', '2026-06-10 23:59:59', 'success', '2026-05-31 09:00:00', 'approved', '2026-05-30 08:30:00', '2026-05-31 09:00:00', NULL, '臺北市信義區松高路11號'),
-- 20: 訂單 90005 待出貨 → 給小農2 測「按已送達」
(20, 9,  1, 3000, 320, '2026-06-15 00:00:00', '2026-06-24 23:59:59', 'success', '2026-06-14 15:00:00', 'approved', '2026-06-13 13:00:00', '2026-06-14 15:00:00', NULL, '桃園市中壢區復興路46號9樓'),
-- 23: 訂單 90006 已完成 → 讓小農1 的團購總收益有金額
(23, 8,  1, 3200, 400, '2026-06-20 00:00:00', '2026-06-29 23:59:59', 'success', '2026-06-19 10:00:00', 'approved', '2026-06-18 09:00:00', '2026-06-19 10:00:00', NULL, '桃園市中壢區復興路46號9樓'),
-- 28: 訂單 90007 待出貨 → 給小農1 測「按已送達」
(28, 1,  3, 2500, 100, '2026-06-25 00:00:00', '2026-07-04 23:59:59', 'success', '2026-06-24 10:00:00', 'approved', '2026-06-23 09:00:00', '2026-06-24 10:00:00', NULL, '臺北市信義區松高路11號'),
-- 29: 訂單 90008 已送達待確認 → 給 user3 的第二筆「確認收貨」
(29, 18, 3, 2000, 120, '2026-07-01 00:00:00', '2026-07-10 23:59:59', 'success', '2026-06-30 11:00:00', 'approved', '2026-06-29 10:00:00', '2026-06-30 11:00:00', NULL, '臺北市信義區松高路11號'),

-- ===== 未成團 2 筆（測「金額未達標，此團購未成團」；未成團不建訂單）=====
(21, 1,  1, 5000, 100, '2026-06-01 00:00:00', '2026-06-10 23:59:59', 'failed', '2026-05-31 10:00:00', 'approved', '2026-05-30 09:00:00', '2026-05-31 10:00:00', NULL, '桃園市中壢區復興路46號9樓'),
(22, 2,  3, 6000, 150, '2026-06-05 00:00:00', '2026-06-14 23:59:59', 'failed', '2026-06-04 13:00:00', 'approved', '2026-06-03 11:00:00', '2026-06-04 13:00:00', NULL, '臺北市信義區松高路11號'),

-- ===== ACTIVE 小農的待審核 / 已拒絕（讓小農後台三個頁籤都有資料）=====
(24, 1,  3, 2000, 100, '2026-07-20 00:00:00', '2026-08-27 23:59:59', 'pending', NULL, 'pending', '2026-07-16 09:00:00', NULL, NULL, '臺北市信義區松高路11號'),
(25, 2,  1, 3000, 150, '2026-07-21 00:00:00', '2026-08-29 23:59:59', 'pending', NULL, 'pending', '2026-07-16 14:00:00', NULL, NULL, '桃園市中壢區復興路46號9樓'),
(26, 8,  3, 3000, 400, '2026-07-05 00:00:00', '2026-07-31 23:59:59', 'cancelled', '2026-07-04 10:00:00', 'rejected', '2026-07-03 09:00:00', '2026-07-04 10:00:00', '該商品近期產量不足，暫時無法開團', '臺北市信義區松高路11號'),
(27, 6,  1, 3000, 280, '2026-07-06 00:00:00', '2026-08-01 23:59:59', 'cancelled', '2026-07-05 11:00:00', 'rejected', '2026-07-04 10:00:00', '2026-07-05 11:00:00', '收貨地址超出可配送範圍', '桃園市中壢區復興路46號9樓');


-- 4. 團購 - 團購參與記錄表
CREATE TABLE GB_PARTICIPATION (
    participation_id INT AUTO_INCREMENT PRIMARY KEY,
    group_buy_id INT,
    user_id INT,
    is_host TINYINT,
    buy_qty INT,
    join_datetime DATETIME,
    join_status ENUM('active','cancelled') DEFAULT 'active',
    paid_amount INT,
    paid_datetime DATETIME,
    FOREIGN KEY (group_buy_id) REFERENCES GROUP_BUY(group_buy_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
-- 規則：paid_amount = buy_qty × 該團的 group_price；每團的 is_host=1 那筆必須是 GROUP_BUY.host_user_id 本人。
-- 待審核(2/4/24/25)與已拒絕(3/26/27)的團購沒有參與紀錄（還沒開團就不會有人參加）。
INSERT INTO GB_PARTICIPATION (participation_id, group_buy_id, user_id, is_host, buy_qty, join_datetime, join_status, paid_amount, paid_datetime) VALUES
-- 團購1（gp 100，團購主 user1）：10+15+5=30 件、3000 元 → 達標 3000
(1,  1,  1, 1, 10, '2026-03-05 08:30:00', 'active', 1000, '2026-03-05 08:35:00'),
(2,  1,  3, 0, 15, '2026-03-06 10:20:00', 'active', 1500, '2026-03-06 10:25:00'),
(3,  1,  2, 0,  5, '2026-03-07 15:10:00', 'active',  500, '2026-03-07 15:15:00'),
-- 開團中：各團皆含團購主本人
(4,  5,  1, 1, 10, '2026-07-10 09:00:00', 'active', 1000, '2026-07-10 09:05:00'),  -- 1800/3000
(5,  5,  3, 0,  8, '2026-07-11 10:00:00', 'active',  800, '2026-07-11 10:05:00'),
(6,  6,  3, 1, 10, '2026-07-10 11:00:00', 'active', 1500, '2026-07-10 11:05:00'),  -- 2250/3000
(7,  6,  1, 0,  5, '2026-07-11 12:00:00', 'active',  750, '2026-07-11 12:05:00'),
(8,  7,  1, 1,  5, '2026-07-11 14:30:00', 'active', 1400, '2026-07-11 14:35:00'),  -- 1400/3000
(9,  8,  3, 1,  5, '2026-07-11 09:30:00', 'active', 2000, '2026-07-11 09:35:00'),  -- 2800/4000
(10, 8,  1, 0,  2, '2026-07-12 10:00:00', 'active',  800, '2026-07-12 10:05:00'),
(11, 9,  1, 1,  4, '2026-07-12 15:30:00', 'active', 1280, '2026-07-12 15:35:00'),  -- 1280/3200
(12, 10, 3, 1, 10, '2026-07-12 11:00:00', 'active', 1200, '2026-07-12 11:05:00'),  -- 1800/2400
(13, 10, 2, 0,  5, '2026-07-13 13:00:00', 'active',  600, '2026-07-13 13:05:00'),
(14, 11, 3, 1,  8, '2026-07-13 16:30:00', 'active',  800, '2026-07-13 16:35:00'),  -- 800/2000
(15, 12, 1, 1,  6, '2026-07-13 13:30:00', 'active',  900, '2026-07-13 13:35:00'),  -- 900/3000
(16, 13, 3, 1, 10, '2026-07-14 10:00:00', 'active', 2800, '2026-07-14 10:05:00'),  -- 2800/5600
(17, 14, 1, 1,  5, '2026-07-14 15:00:00', 'active', 2000, '2026-07-14 15:05:00'),  -- 2000/4000
(18, 15, 3, 1,  5, '2026-07-15 10:30:00', 'active', 1600, '2026-07-15 10:35:00'),  -- 1600/3200
(19, 16, 1, 1, 12, '2026-07-15 11:30:00', 'active', 1440, '2026-07-15 11:35:00'),  -- 1440/1200 已達標
-- 已成團：加總必須等於對應訂單的 total_quantity / total_amount
(20, 17, 3, 1, 12, '2026-05-02 10:00:00', 'active', 1800, '2026-05-02 10:05:00'),  -- 18 件 / 2700
(21, 17, 1, 0,  6, '2026-05-03 11:00:00', 'active',  900, '2026-05-03 11:05:00'),
(22, 18, 1, 1,  8, '2026-05-16 09:00:00', 'active', 2240, '2026-05-16 09:05:00'),  -- 11 件 / 3080
(23, 18, 3, 0,  3, '2026-05-17 14:00:00', 'active',  840, '2026-05-17 14:05:00'),
(24, 19, 3, 1,  6, '2026-06-02 10:00:00', 'active', 2400, '2026-06-02 10:05:00'),  -- 9 件 / 3600
(25, 19, 1, 0,  3, '2026-06-03 13:00:00', 'active', 1200, '2026-06-03 13:05:00'),
(26, 20, 1, 1,  7, '2026-06-16 09:30:00', 'active', 2240, '2026-06-16 09:35:00'),  -- 10 件 / 3200
(27, 20, 3, 0,  3, '2026-06-17 15:00:00', 'active',  960, '2026-06-17 15:05:00'),
(28, 23, 1, 1,  5, '2026-06-21 10:00:00', 'active', 2000, '2026-06-21 10:05:00'),  -- 8 件 / 3200
(29, 23, 3, 0,  3, '2026-06-22 11:00:00', 'active', 1200, '2026-06-22 11:05:00'),
(33, 28, 3, 1, 18, '2026-06-26 09:00:00', 'active', 1800, '2026-06-26 09:05:00'),  -- 26 件 / 2600
(34, 28, 1, 0,  8, '2026-06-27 10:00:00', 'active',  800, '2026-06-27 10:05:00'),
(35, 29, 3, 1, 12, '2026-07-02 10:00:00', 'active', 1440, '2026-07-02 10:05:00'),  -- 17 件 / 2040
(36, 29, 2, 0,  5, '2026-07-03 11:00:00', 'active',  600, '2026-07-03 11:05:00'),
-- 未成團：金額沒到 target，所以不會有訂單
(30, 21, 1, 1, 10, '2026-06-02 09:00:00', 'active', 1000, '2026-06-02 09:05:00'),  -- 1500/5000 未達標
(31, 21, 3, 0,  5, '2026-06-03 10:00:00', 'active',  500, '2026-06-03 10:05:00'),
(32, 22, 3, 1,  8, '2026-06-06 14:00:00', 'active', 1200, '2026-06-06 14:05:00');  -- 1200/6000 未達標


-- 4. 團購 - 團購訂單
CREATE TABLE GB_ORDER (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    group_buy_id INT,
    total_quantity INT,
    group_price INT,
    total_amount INT,
    shipped_status ENUM('PENDING','DELIVERED') DEFAULT 'PENDING',
    shipped_at DATETIME,
    created_at DATETIME,
    received_at DATETIME,
    order_status ENUM('PENDING', 'COMPLETED'),
    paid_status ENUM('UNPAID', 'PAID'),
    completed_at DATETIME,
    FOREIGN KEY (group_buy_id) REFERENCES GROUP_BUY(group_buy_id)


) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
-- 出貨流程：小農按「已送達」→ shipped_status=DELIVERED → 團購主才能按「確認收貨」→ order_status=COMPLETED + paid_status=PAID。
-- 規則：
--  1. 只有 status='success' 的團購才有訂單；total_quantity / total_amount 必須等於該團參與紀錄的加總。
--  2. shipped_status='PENDING'（待出貨）時團購主不能確認收貨（confirmReceipt() 會擋：「商品尚未出貨，無法確認收貨」），
--     所以要測「確認收貨」的訂單必須先是 DELIVERED + order_status='PENDING'。
--  3. order_status='COMPLETED' 必然是 shipped_status='DELIVERED' + paid_status='PAID'（確認收貨時一次設定）。
--  4. order_status='PENDING' 時 paid_status 必為 'UNPAID'——撥款只發生在團購主確認收貨之後。
--  5. shipped_at 只有 DELIVERED 才有值；received_at / completed_at 只有 COMPLETED 才有值。
INSERT INTO GB_ORDER (order_id, group_buy_id, total_quantity, group_price, total_amount, shipped_status, shipped_at, created_at, received_at, order_status, paid_status, completed_at) VALUES
-- 已送達待確認（小農1 出貨、團購主 user1 可確認收貨）：notification 種子有引用此訂單編號
(90001, 1,  30, 100, 3000, 'DELIVERED', '2026-03-14 10:00:00', '2026-03-12 23:59:59', NULL,                  'PENDING',   'UNPAID', NULL),
-- 已完成（小農2）
(90002, 17, 18, 150, 2700, 'DELIVERED', '2026-05-12 10:00:00', '2026-05-10 23:59:59', '2026-05-13 15:00:00', 'COMPLETED', 'PAID',   '2026-05-13 15:30:00'),
(90003, 18, 11, 280, 3080, 'DELIVERED', '2026-05-26 09:30:00', '2026-05-24 23:59:59', '2026-05-27 14:20:00', 'COMPLETED', 'PAID',   '2026-05-27 15:00:00'),
-- 已送達待確認（小農1 出貨、團購主 user3 可確認收貨）
(90004, 19,  9, 400, 3600, 'DELIVERED', '2026-06-12 09:30:00', '2026-06-10 23:59:59', NULL,                  'PENDING',   'UNPAID', NULL),
-- 待出貨（小農2 可按「已送達」；此時團購主 user1 還不能確認收貨）
(90005, 20, 10, 320, 3200, 'PENDING',   NULL,                  '2026-06-24 23:59:59', NULL,                  'PENDING',   'UNPAID', NULL),
-- 已完成（小農1）→ 讓小農1 的團購總收益有金額
(90006, 23,  8, 400, 3200, 'DELIVERED', '2026-07-01 09:00:00', '2026-06-29 23:59:59', '2026-07-02 12:00:00', 'COMPLETED', 'PAID',   '2026-07-02 13:00:00'),
-- 待出貨（小農1 可按「已送達」）
(90007, 28, 26, 100, 2600, 'PENDING',   NULL,                  '2026-07-04 23:59:59', NULL,                  'PENDING',   'UNPAID', NULL),
-- 已送達待確認（小農2 出貨、團購主 user3 可確認收貨）
(90008, 29, 17, 120, 2040, 'DELIVERED', '2026-07-12 10:00:00', '2026-07-10 23:59:59', NULL,                  'PENDING',   'UNPAID', NULL);


-- 4. 團購 - 團購收藏表
CREATE TABLE GB_WISHLIST (
    user_id INT,
    group_buy_id INT,
    saved_datetime DATETIME,
    PRIMARY KEY (user_id, group_buy_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (group_buy_id) REFERENCES GROUP_BUY(group_buy_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
-- 規則：只能收藏 status='open' 的團購。
--  後端 showGroupBuyFavoriteList() 只回 open 的，groupBuyFavorite() 新增時也會擋非 open，
--  所以指向 success/failed/pending 的收藏既查不到、也不可能經由 API 建立，不要放。
--  user1 收藏 11 筆 → 我的收藏一頁 10 筆，可測到第 2 頁。
INSERT INTO GB_WISHLIST (
    user_id,
    group_buy_id,
    saved_datetime
) VALUES
(1, 5,  '2026-07-10 09:30:00'),
(1, 6,  '2026-07-10 11:30:00'),
(1, 7,  '2026-07-11 15:00:00'),
(1, 8,  '2026-07-11 10:00:00'),
(1, 9,  '2026-07-12 16:00:00'),
(1, 10, '2026-07-12 11:30:00'),
(1, 11, '2026-07-13 17:00:00'),
(1, 12, '2026-07-13 14:00:00'),
(1, 13, '2026-07-14 10:30:00'),
(1, 14, '2026-07-14 15:30:00'),
(1, 15, '2026-07-15 11:00:00'),
(3, 5,  '2026-07-10 10:00:00'),
(3, 16, '2026-07-15 12:00:00');



-- ==========================================
-- 四、體驗活動模組 (依賴 Farmer, User, Admin)
-- ==========================================

-- 5. 體驗活動 - 體驗活動
CREATE TABLE FARM_TRIP (
    farm_trip_id INT AUTO_INCREMENT PRIMARY KEY,
    farmer_id INT NOT NULL,
    farm_trip_type ENUM('FARM_EXPERIENCE','FIELD_VISIT'),
    farm_trip_title VARCHAR(30),
    farm_trip_pic LONGBLOB,
    farm_trip_intro VARCHAR(500),
    location VARCHAR(100),
    refer_price INT,
    trip_status ENUM('PENDING','REJECTED','ACTIVE','CLOSED'),
    comment_numbers INT,
    star_numbers INT,
    FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP (farm_trip_id, farmer_id, farm_trip_type, farm_trip_title, farm_trip_pic, farm_trip_intro, location, refer_price, trip_status, comment_numbers, star_numbers) VALUES
(5001, 1, 'FARM_EXPERIENCE','有機蔬菜採收體驗',NULL,'走進陽光農場認識有機蔬菜，親手完成採收與清洗。','臺北市中正區中正路500號',500, 'ACTIVE', 1, 5),

(5002, 2, 'FIELD_VISIT','自然農法農場導覽',NULL,'由農友介紹自然農法、土壤照護及友善環境的栽培方式。','臺北市大同區大同街200號',300, 'ACTIVE', 1, 5),

(5003, 2, 'FARM_EXPERIENCE','當季蔬果採收體驗',NULL,'親手採收當季蔬果，學習辨認成熟度及正確保存方法。','臺北市大同區大同街200號', 450, 'ACTIVE', 1, 5),

(5004, 3, 'FARM_EXPERIENCE','高山茶採摘製茶體驗', NULL, '體驗茶葉採摘、揉捻與烘焙，認識茶葉從產地到茶杯的過程。','臺北市中山區中山路333號', 800, 'ACTIVE', 1, 4),

(5005, 5, 'FARM_EXPERIENCE','稻田插秧與米食體驗',NULL,'走入稻田體驗插秧，並認識稻米生長及傳統米食製作。','新北市板橋區板橋路77號',600, 'ACTIVE', 1, 4);



-- 5. 體驗活動 - 體驗活動場次
CREATE TABLE FARM_TRIP_SESSION (
    farm_session_id INT AUTO_INCREMENT PRIMARY KEY,
    farm_trip_id INT NOT NULL,
    farm_trip_start DATETIME,
    farm_trip_end DATETIME,
    trip_book_start DATETIME,
    trip_book_end DATETIME,
    attendance INT,
    session_status ENUM('ACTIVE','CANCELLED','COMPLETED'),
    FOREIGN KEY (farm_trip_id) REFERENCES FARM_TRIP(farm_trip_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP_SESSION (farm_session_id, farm_trip_id, farm_trip_start, farm_trip_end, trip_book_start, trip_book_end, attendance, session_status) VALUES
(6001, 5001,
 '2026-04-10 09:00:00', '2026-04-10 15:00:00','2026-03-10 00:00:00', '2026-04-08 23:59:59',30, 'COMPLETED'),

(6002, 5002, '2026-04-17 09:00:00', '2026-04-17 12:00:00', '2026-03-15 00:00:00', '2026-04-15 23:59:59', 25, 'COMPLETED'),

(6003, 5003, '2026-04-24 08:30:00', '2026-04-24 14:00:00', '2026-03-20 00:00:00', '2026-04-22 23:59:59', 20, 'COMPLETED'),

(6004, 5004, '2026-05-08 08:00:00', '2026-05-08 16:00:00', '2026-04-01 00:00:00', '2026-05-06 23:59:59', 15, 'COMPLETED'),

(6005, 5005, '2026-05-15 09:00:00', '2026-05-15 16:00:00', '2026-04-05 00:00:00', '2026-05-13 23:59:59', 20, 'COMPLETED');


-- 5. 體驗活動 - 體驗活動審核
CREATE TABLE FARM_TRIP_AUDITS (
    farm_trip_audits_id INT AUTO_INCREMENT PRIMARY KEY,
    farm_trip_id INT NOT NULL,
    admin_id INT NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED'),
    reason VARCHAR(255),
    created_at DATETIME,
    updated_at DATETIME,
    FOREIGN KEY (farm_trip_id) REFERENCES FARM_TRIP(farm_trip_id),
    FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP_AUDITS (farm_trip_audits_id, farm_trip_id, admin_id, status, reason, created_at, updated_at) VALUES
(7001, 5001, 1, 'APPROVED', '活動流程完整，場地資訊清楚。', '2026-03-05 14:00:00', '2026-03-05 14:00:00'),

(7002, 5002, 2, 'APPROVED', '導覽內容及安全規劃符合要求。', '2026-03-10 10:00:00', '2026-03-10 11:00:00'),

(7003, 5003, 1, 'APPROVED','採收流程與參加規範說明完整。','2026-03-15 09:00:00', '2026-03-15 10:30:00'),

(7004, 5004, 3, 'APPROVED','製茶體驗設備及安全措施符合規定。','2026-03-25 13:00:00', '2026-03-25 15:00:00'),

(7005, 5005, 2, 'APPROVED','插秧與米食活動流程規劃完善。', '2026-03-30 10:00:00', '2026-03-30 12:00:00');


-- 5. 體驗活動 - 體驗活動預約訂單
CREATE TABLE FARM_TRIP_ORDER (
    farm_trip_order_id INT AUTO_INCREMENT PRIMARY KEY,
    farm_session_id INT NOT NULL,
    user_id INT NOT NULL,
    farm_trip_order_booking_no VARCHAR(30),
    num_people INT NOT NULL,
    order_status ENUM('CONFIRMED','CANCELLED','COMPLETED'),
    booked_at DATETIME NOT NULL,
    cancelled_at DATETIME,
    completed_at DATETIME,
    user_name VARCHAR(30) NOT NULL,
    user_phone_num VARCHAR(15) NOT NULL,
    note VARCHAR(100),
    FOREIGN KEY (farm_session_id) REFERENCES FARM_TRIP_SESSION(farm_session_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP_ORDER (farm_trip_order_id, farm_session_id, user_id, farm_trip_order_booking_no, num_people, order_status, booked_at, cancelled_at, completed_at, user_name, user_phone_num, note) VALUES
(8001, 6001, 5, 'TRIP-20260410-001',2, 'COMPLETED','2026-03-15 10:00:00', NULL, '2026-04-10 15:10:00','黃雅婷', '0956789012','第一次參加農村體驗，希望有導覽說明。'),

(8002, 6002, 1, 'TRIP-20260417-001', 2, 'COMPLETED', '2026-03-20 11:00:00', NULL, '2026-04-17 12:10:00', '陳小美', '0912345678', '希望了解自然農法與土壤照護。'),

(8003, 6003, 2, 'TRIP-20260424-001', 3, 'COMPLETED', '2026-03-25 14:30:00', NULL, '2026-04-24 14:10:00', '林大明', '0923456789', '三人同行，想體驗當季蔬果採收。'),

(8004, 6004, 3, 'TRIP-20260508-001', 1, 'COMPLETED', '2026-04-10 09:20:00', NULL, '2026-05-08 16:10:00', '王小華', '0934567890', '對茶葉製作過程很有興趣。'),

(8005, 6005, 5, 'TRIP-20260515-001', 2, 'COMPLETED', '2026-04-15 16:00:00', NULL, '2026-05-15 16:10:00', '黃雅婷', '0956789012', '希望體驗插秧與傳統米食製作。');
-- 5. 體驗活動 - 體驗活動評論
CREATE TABLE FARM_TRIP_COMMENT (
    farm_trip_comment INT AUTO_INCREMENT PRIMARY KEY,
    farm_trip_id INT NOT NULL,
    user_id INT NOT NULL,
    star INT,
    content VARCHAR(255),
    created_at DATETIME,
    FOREIGN KEY (farm_trip_id) REFERENCES FARM_TRIP(farm_trip_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO FARM_TRIP_COMMENT (farm_trip_comment, farm_trip_id, user_id, star, content, created_at) VALUES
(1, 5001, 5, 5, '有機蔬菜採收體驗很有趣，農友解說得非常仔細。', '2026-04-11 10:00:00'),

(2, 5002, 1, 4, '導覽內容很豐富，讓我更了解自然農法。', '2026-04-18 11:00:00'),

(3, 5003, 2, 5, '親手採收蔬果非常有成就感，適合親子參加。', '2026-04-25 09:30:00'),

(4, 5004, 3, 5, '從採茶到製茶都能親自參與，是很難得的體驗。', '2026-05-09 10:00:00'),

(5, 5005, 5, 4, '插秧很有趣，米食製作活動也很好吃。', '2026-05-16 11:00:00');


-- ==========================================
-- 五、專欄部落格模組 (依賴 User, Farmer, Product, Admin, Blog_Type)
-- ==========================================

-- 6. 專欄部落格 - 部落格文章
CREATE TABLE BLOG (
    blog_id INT AUTO_INCREMENT PRIMARY KEY,
    blog_title VARCHAR(30) NOT NULL,
    user_id INT,
    farmer_id INT,
    blog_type_id INT,
    blog_content TEXT NOT NULL,
    blog_img LONGBLOB,
    blog_like_count INT NOT NULL,
    blog_time DATETIME NOT NULL,
    blog_status ENUM('VISIBLE','HIDDEN'),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (farmer_id) REFERENCES FARMER(farmer_id),
    FOREIGN KEY (blog_type_id) REFERENCES BLOG_TYPE(blog_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG (blog_id, blog_title, user_id, farmer_id, blog_type_id, blog_content, blog_img, blog_like_count, blog_time, blog_status) VALUES
-- 一根香蕉
(9001, '一根香蕉的產地旅程',NULL, 1, 1,'從清晨採收到冷鏈配送，每一根香蕉都承載著小農的用心。', NULL, 48, '2026-02-20 14:00:00', 'VISIBLE'),
-- 產地日記
(9002, '有機農場的一天',NULL, 1, 1,'農友從整地、灌溉到採收，每個步驟都堅持友善土地與自然栽培。', NULL, 32, '2026-02-21 09:30:00', 'VISIBLE'),

-- 蔬果知識分享
(9003, '當季蔬果怎麼挑', 4, NULL, 2,'<p>選購蔬果時，除了注意價格，也可以從外觀、香氣、觸感與採收日期來判斷新鮮度。挑選當季盛產的蔬果，不僅風味通常更好，價格也比較實惠，還能減少長途運輸與長時間保存造成的品質下降。</p><p>首先可以觀察蔬果的外觀。新鮮的葉菜類通常葉片完整、顏色自然，沒有大面積枯黃或腐爛；番茄、甜椒等果菜類則應表皮飽滿，避免選擇有明顯裂痕、碰傷或發霉的產品。不過，外觀不漂亮不代表品質不好，有些小農蔬果雖然大小不一或帶有自然斑點，仍然新鮮又美味。</p><p>接著可以用手輕輕觸摸。根莖類蔬菜如馬鈴薯、紅蘿蔔，摸起來應該結實，不宜過度柔軟；葉菜類的菜梗則應保持挺立、有水分。挑選時不要用力按壓，以免造成蔬果損傷，影響後續販售與保存。</p><p>香氣也是判斷成熟度的方法之一。例如成熟的水果通常會散發自然果香，但如果味道過於濃烈，或帶有酒味、酸敗味，就可能已經過熟。部分蔬菜本身香氣不明顯，因此仍需要搭配外觀與觸感一起判斷。</p><p>購買前也可以詢問農友蔬果的採收日期、產地與建議保存方式。剛採收的蔬果不一定要立刻冷藏，有些品項適合放在陰涼通風處，有些則需要冷藏保鮮。依照不同蔬果的特性保存，才能延長新鮮度並減少浪費。</p><p>最後，建議優先選擇當地、當季生產的蔬果。當季作物通常較適合當下的氣候環境，也能品嘗到自然成熟的風味。透過認識產地與農友，不只可以買得更安心，也能用實際行動支持台灣在地農業。</p>', NULL, 65, '2026-02-22 11:00:00', 'VISIBLE'),

-- 農作體驗回顧，由一般會員發表
(9004, '山間農場參訪記', 1, NULL, 3,'第一次走進山間農場，親手體驗採收，也更了解農作物從產地到餐桌的過程。', NULL, 41, '2026-02-23 15:20:00', 'VISIBLE'),

-- 農作體驗回顧，由一般會員發表
(9005, '海風農場體驗日', 2, NULL, 3, '迎著海風參觀農場，除了認識不同的栽培方式，也體會到農友工作的辛苦。', NULL, 27, '2026-02-24 10:10:00', 'VISIBLE'),

-- 食譜分享
(9006, '香蕉燕麥鬆餅', 5, NULL, 4, '將熟香蕉壓成泥，加入雞蛋與燕麥拌勻，再用平底鍋煎成香甜鬆餅。', NULL, 53, '2026-02-25 13:40:00', 'VISIBLE'),

-- 產地日記：只能由小農發表（user_id 必須為 NULL）
(9007, '春耕前的土壤準備', NULL, 1, 1, '<p>春耕開始前，我們先翻鬆休耕一段時間的田地，清除雜草並檢查土壤的排水狀況。接著加入腐熟堆肥，讓有機質慢慢與土壤混合。</p><p>整地完成後不會立刻播種，而是讓土地休息幾天，再依照含水量調整畦面的高度。把基礎做好，作物後續的根系才會長得穩定。</p>', NULL, 18, '2026-03-01 08:30:00', 'VISIBLE'),
(9008, '雨後巡田與排水紀錄', NULL, 2, 1, '<p>昨晚的大雨讓田間水位上升，今天一早先巡查排水溝與每一區作物。低窪處有些積水，我們立即清除堵住溝渠的落葉，避免根部長時間泡水。</p><p>雨後也是觀察病蟲害的重要時機。除了修剪受損葉片，也記錄各區狀況，等天氣穩定後再補強支架與土壤。</p>', NULL, 24, '2026-03-03 07:50:00', 'VISIBLE'),

-- 蔬果知識分享：由一般會員發表（farmer_id 必須為 NULL）
(9009, '夏季葉菜保存小技巧', 1, NULL, 2, '<p>葉菜買回家後先挑掉受損葉片，不要急著全部清洗。可以用廚房紙巾包住菜葉，再放入保鮮袋中冷藏，減少水分散失。</p><p>如果已經清洗，記得先瀝乾表面水分。保存時避免把葉菜壓在重物下方，並盡量在幾天內食用完畢。</p>', NULL, 31, '2026-03-05 19:20:00', 'VISIBLE'),
(9010, '甜玉米怎麼挑才好吃', 3, NULL, 2, '<p>挑選甜玉米時，可以先觀察外葉是否翠綠、苞葉是否緊密。玉米鬚呈現褐色但不過度乾枯，通常代表成熟度較合適。</p><p>輕壓玉米粒時應飽滿有彈性，若出現明顯凹陷，可能已經失去部分水分。買回家後越早烹煮，越能保留甜味。</p>', NULL, 22, '2026-03-07 12:10:00', 'VISIBLE'),
(9011, '根莖類蔬菜保存指南', 4, NULL, 2, '<p>馬鈴薯與地瓜適合放在陰涼、乾燥且通風的地方，避免陽光直射。若放進密閉塑膠袋，水氣不易散出，反而比較容易發芽或腐壞。</p><p>紅蘿蔔則可以去除葉梗後冷藏，並用紙巾吸收多餘水分。不同根莖類的保存方式不完全相同，分開收納會更理想。</p>', NULL, 16, '2026-03-09 18:40:00', 'VISIBLE'),

-- 農作體驗回顧：由一般會員發表（farmer_id 必須為 NULL）
(9012, '第一次參加稻田插秧', 4, NULL, 3, '<p>這次第一次踩進水田，才發現插秧不只是把秧苗放進土裡，還要控制間距與深度。剛開始常常站不穩，後來跟著農友的示範才慢慢抓到節奏。</p><p>活動結束後，看著整齊排列的秧苗很有成就感，也更能體會一碗米飯背後需要投入多少時間與心力。</p>', NULL, 45, '2026-03-12 16:30:00', 'VISIBLE'),
(9013, '親子採番茄體驗心得', 5, NULL, 3, '<p>週末帶家人參加採番茄活動，農友先介紹成熟番茄的顏色與正確採摘方式，避免拉扯枝條。孩子一邊找果實，一邊認識作物的生長過程。</p><p>現場也能品嘗不同品種的番茄，酸甜度各有特色。整體活動安排清楚，很適合第一次接觸農作體驗的人。</p>', NULL, 38, '2026-03-14 17:15:00', 'VISIBLE'),

-- 食譜分享：由一般會員發表（farmer_id 必須為 NULL）
(9014, '三杯杏鮑菇家常做法', 1, NULL, 4, '<p>先將杏鮑菇切成滾刀塊，用少量油煎到表面金黃。加入薑片、蒜頭、醬油與少許糖拌炒，再放入九層塔增添香氣。</p><p>杏鮑菇本身會出水，調味料不需要一次加太多。收汁後即可起鍋，是簡單又下飯的家常料理。</p>', NULL, 57, '2026-03-17 11:45:00', 'VISIBLE'),
(9015, '南瓜濃湯簡單食譜', 3, NULL, 4, '<p>南瓜去籽切塊後蒸熟，與炒軟的洋蔥一起放入果汁機，加入適量牛奶或無糖豆漿打勻。倒回鍋中用小火加熱，再以鹽和胡椒調味。</p><p>喜歡濃稠口感可以減少液體，想要清爽一些則多加少量水。南瓜本身有甜味，不需要額外加糖。</p>', NULL, 49, '2026-03-19 20:05:00', 'VISIBLE'),
(9016, '高麗菜豬肉水餃', 5, NULL, 4, '<p>高麗菜切碎後加少量鹽抓勻，靜置幾分鐘再擠掉多餘水分。豬絞肉加入醬油、薑末與白胡椒攪拌，最後混入高麗菜。</p><p>包好的水餃可以直接下鍋，也能平鋪冷凍後分袋保存。餡料水分控制得好，煮的時候比較不容易破皮。</p>', NULL, 62, '2026-03-21 18:25:00', 'VISIBLE');

-- 6. 專欄部落格 - 按讚檢查
CREATE TABLE BLOG_LIKE (
    blog_id INT NOT NULL,
    user_id INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (blog_id, user_id),

    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO BLOG_LIKE (blog_id, user_id, created_at) value
(9001, 1 ,'2026-06-04 14:00:00') ,
(9002, 2, '2026-02-21 12:00:00'),
(9003, 3, '2026-02-22 14:00:00'),
(9004, 4, '2026-02-23 17:00:00'),
(9006, 1, '2026-02-25 16:00:00');

-- 9007~9016 新增文章的按讚（(blog_id,user_id) 不重複）
INSERT INTO BLOG_LIKE (blog_id, user_id, created_at) VALUES
(9007, 2, '2026-03-01 21:00:00'),
(9008, 3, '2026-03-03 19:00:00'),
(9009, 2, '2026-03-06 09:00:00'),
(9010, 1, '2026-03-07 22:00:00'),
(9012, 5, '2026-03-13 11:00:00'),
(9014, 4, '2026-03-18 20:00:00'),
(9015, 1, '2026-03-20 09:00:00'),
(9016, 3, '2026-03-22 14:00:00');

-- 6. 專欄部落格 - 部落格評論
CREATE TABLE BLOG_COMMENT (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    blog_id INT NOT NULL,
    user_id INT NOT NULL,
    comment_time DATETIME,
    comment_post VARCHAR(200) NOT NULL,
    comment_like INT NOT NULL,
    comment_status ENUM('VISIBLE','HIDDEN'),
    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_COMMENT (comment_id, blog_id, user_id, comment_time, comment_post, comment_like, comment_status) VALUES
(10001, 9001, 1, '2026-02-21 09:00:00', '看完更想支持在地小農，內容很有溫度。', 5, 'VISIBLE'),

(10002, 9002, 2, '2026-02-21 13:00:00', '原來有機農場每天需要做這麼多工作！', 3, 'VISIBLE'),

(10003, 9003, 3, '2026-02-22 15:30:00','這些挑選蔬果的方法很實用。', 4, 'VISIBLE'),

(10004, 9004, 4, '2026-02-23 18:00:00', '看完也想親自參加農場體驗。', 2, 'VISIBLE'),

(10005, 9005, 5, '2026-02-24 14:00:00','農場環境看起來很舒服。', 1, 'VISIBLE'),

(10006, 9006, 1, '2026-02-25 18:30:00', '做法簡單，下次想在家試試看。', 5, 'VISIBLE');

-- 9007~9016 新增文章的留言（避免留言者＝作者本人）
INSERT INTO BLOG_COMMENT (comment_id, blog_id, user_id, comment_time, comment_post, comment_like, comment_status) VALUES
(10007, 9007, 2, '2026-03-01 20:15:00', '整地做得這麼細，難怪作物長得好。', 3, 'VISIBLE'),
(10008, 9007, 3, '2026-03-02 09:40:00', '每個步驟都很用心，看了很感動。', 1, 'VISIBLE'),
(10009, 9008, 4, '2026-03-03 18:05:00', '雨後巡田真的很重要，學到了。', 2, 'VISIBLE'),
(10010, 9009, 2, '2026-03-06 08:30:00', '廚房紙巾這招我要來試試。', 4, 'VISIBLE'),
(10011, 9010, 1, '2026-03-07 21:00:00', '下次買玉米知道怎麼挑了，謝謝分享。', 2, 'VISIBLE'),
(10012, 9011, 2, '2026-03-10 12:20:00', '原來馬鈴薯不能放塑膠袋，難怪常發芽。', 5, 'VISIBLE'),
(10013, 9012, 1, '2026-03-13 10:10:00', '看起來好好玩，也想帶小孩去體驗。', 3, 'VISIBLE'),
(10014, 9013, 2, '2026-03-15 09:25:00', '親子活動的好選擇，記錄得很詳細。', 2, 'VISIBLE'),
(10015, 9014, 4, '2026-03-18 19:45:00', '素食版三杯也太香，收藏了！', 6, 'VISIBLE'),
(10016, 9015, 5, '2026-03-20 08:15:00', '作法簡單又暖胃，冬天必備。', 3, 'VISIBLE'),
(10017, 9016, 1, '2026-03-22 13:30:00', '自己包的水餃就是不一樣，讚！', 4, 'VISIBLE');


-- 6. 專欄部落格 - 部落格照片
CREATE TABLE BLOG_PHOTO (
    blog_photo_id INT AUTO_INCREMENT PRIMARY KEY,
    blog_id INT NOT NULL,
    blog_photo LONGBLOB,
    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_PHOTO (blog_photo_id, blog_id, blog_photo) VALUES
(1, 9001, NULL),
(2, 9002, NULL),
(3, 9003, NULL),
(4, 9004, NULL),
(5, 9005, NULL),
(6, 9006, NULL);


-- 6. 專欄部落格 - 部落格檢舉
CREATE TABLE BLOG_REPORT (
    blog_report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    blog_id INT NOT NULL,
    admin_id INT,
    report_time DATETIME,
    report_reason VARCHAR(100),
    report_status ENUM('PENDING', 'APPROVED_VISIBLE', 'REJECTED_HIDDEN'),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id),
    FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_REPORT (blog_report_id, user_id, blog_id, admin_id, report_time, report_reason, report_status) VALUES
(12001,1 ,9001, NULL, '2026-02-22 18:00:00', '內容疑似與商品資訊不符，請平台確認。', 'PENDING');


-- 6. 專欄部落格 - 部落格評論檢舉
CREATE TABLE BLOG_COMMENT_REPORT (
    report_comment_id INT AUTO_INCREMENT PRIMARY KEY,
    comment_id INT NOT NULL,
    blog_id INT NOT NULL,
    user_id INT NOT NULL,
    admin_id INT,
    report_time DATETIME,
    report_reason VARCHAR(100),
    report_status ENUM('PENDING', 'APPROVED_VISIBLE', 'REJECTED_HIDDEN'),
    FOREIGN KEY (comment_id) REFERENCES BLOG_COMMENT(comment_id),
    FOREIGN KEY (blog_id) REFERENCES BLOG(blog_id),
    FOREIGN KEY (user_id) REFERENCES USER(user_id),
    FOREIGN KEY (admin_id) REFERENCES ADMIN(admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 參數
INSERT INTO BLOG_COMMENT_REPORT (report_comment_id,comment_id, blog_id, user_id, admin_id, report_time, report_reason, report_status) VALUES
(13001, 10001, 9001, 3, NULL, '2026-02-22 19:00:00', '留言內容可能涉及不當評論。', 'PENDING');


-- 7. 通知 - 通知
DROP TABLE IF EXISTS notification;
CREATE TABLE IF NOT EXISTS notification (
notification_id INT NOT NULL AUTO_INCREMENT, 
recipient_type ENUM('user', 'farmer', 'admin') NOT NULL,
recipient_id INT NOT NULL, 
type_code VARCHAR(40),
target_type VARCHAR(40),
target_id INT,
content VARCHAR(500) NOT NULL, 
status ENUM('unread', 'read') NOT NULL DEFAULT 'unread', 
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
CONSTRAINT PK_NOTIFICATION_NID PRIMARY KEY (notification_id)
)
AUTO_INCREMENT = 7001,
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_notif_recipient_status 
ON notification (recipient_type, recipient_id, status);

INSERT INTO notification (recipient_type, recipient_id, type_code, target_type, target_id, content) VALUES ('user', 3, 'gb_success', 'groupbuy', '1', '團購編號 1 已成團，訂單編號 90001，將盡速為您安排出貨。');
INSERT INTO notification (recipient_type, recipient_id, type_code, target_type, target_id, content) VALUES ('farmer', 1, 'order_farmer_new', 'order', 1, '您有一筆新商品訂單 1，請盡速出貨。');
INSERT INTO notification (recipient_type, recipient_id, type_code, target_type, target_id, content) VALUES ('admin', 1, 'farmer_review', 'account', 5 , '小農申請編號 5，請盡速審核。');
INSERT INTO notification (recipient_type, recipient_id, type_code, target_type, target_id, content) VALUES ('admin', 3, 'blog_report_new', 'blog', 9001, '專欄文章 一根香蕉的產地旅程 遭檢舉，檢舉事由: 內容疑似與商品資訊不符，請平台確認。請盡速審核。');

-- 參數
INSERT INTO notification (recipient_type, recipient_id, type_code, target_type, target_id, content) VALUES ('user', 3, 'blog_comment', 'blog', '9015', '您的文章 南瓜濃湯簡單食譜 收到一則新留言。');
INSERT INTO notification (recipient_type, recipient_id, type_code, target_type, target_id, content) VALUES ('user', 3, 'trip_booking_confirmed', 'trip', '8004', '已收到您的體驗活動預約 高山茶採摘製茶體驗，期待您的到來！');
INSERT INTO notification (recipient_type, recipient_id, type_code, target_type, target_id, content) VALUES ('user', 3, 'trip_review_invite', 'trip', '5004', '感謝您參加體驗活動 高山茶採摘製茶體驗！歡迎留下您的評分與心得，給小農鼓勵。');
INSERT INTO notification (recipient_type, recipient_id, type_code, target_type, target_id, content) VALUES ('user', 3, 'order_created', 'order', '1', '已收到您的商品訂單 1，將盡速為您安排出貨');



-- 7. 通知 - 通知類型文字模板
DROP TABLE IF EXISTS notification_template;
DROP TABLE IF EXISTS notification_type;
CREATE TABLE IF NOT EXISTS notification_template (
type_code VARCHAR(40) NOT NULL, 
template_zh VARCHAR(255) NOT NULL, 
CONSTRAINT PK_NOTIFYTYPE_CODE PRIMARY KEY (type_code)
)
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO notification_template VALUES ('gb_request_approved', '團購編號 {group_buy_id} 的開團申請已通過，快邀請親朋好友一起參加!');
INSERT INTO notification_template VALUES ('gb_request_rejected', '團購編號 {group_buy_id} 的開團申請未通過，原因：{reject_reason}。');
INSERT INTO notification_template VALUES ('gb_success', '團購編號 {group_buy_id} 已成團，訂單編號 {order_id}，將盡速為您安排出貨。');
INSERT INTO notification_template VALUES ('gb_failed', '團購編號 {group_buy_id} 未達標，活動已取消，款項將全額退還，期待您再次參與。');
INSERT INTO notification_template VALUES ('gb_cancelled', '團購編號 {group_buy_id} 已取消，款項將全額退還。');
INSERT INTO notification_template VALUES ('gb_shipped', '團購訂單編號 {order_id} 商品已於 {shipped_at} 出貨，請留意近期收貨。');
INSERT INTO notification_template VALUES ('gb_delivered', '團購訂單編號 {order_id} 商品已於 {received_at} 確認配達，感謝您的參與，快聯繫團員取貨吧!');
INSERT INTO notification_template VALUES ('gb_request_created', '您收到 {product_name} 的新團購申請，請儘速審核回覆。');
INSERT INTO notification_template VALUES ('gb_order_created', '團購訂單 {group_buy_id} 已成團，請準備出貨。');
INSERT INTO notification_template VALUES ('gb_payout', '團購訂單 {order_id} 已確認收貨，貨款 NT$ {total_amount} 已撥款，請查收。');

INSERT INTO notification_template VALUES ('order_created', '已收到您的商品訂單 {order_id}，將盡速為您安排出貨');
INSERT INTO notification_template VALUES ('order_shipped', '商品訂單編號 {order_id} 已於 {shipped_at} 出貨，請留意近期收貨。');
INSERT INTO notification_template VALUES ('order_received', '商品訂單編號 {order_id} 已於 {received_at} 配達，感謝您的購買。');
INSERT INTO notification_template VALUES ('order_farmer_new', '您有一筆新商品訂單 {order_id}，請盡速出貨。');
INSERT INTO notification_template VALUES ('order_payout', '商品訂單 {order_id} 買家已確認收貨，貨款已撥款，請查收。');

INSERT INTO notification_template VALUES ('trip_request_approved', '體驗活動 {farm_trip_title} 已通過審核並開放報名，快邀請親朋好友一起參加!');
INSERT INTO notification_template VALUES ('trip_request_rejected', '體驗活動 {farm_trip_title} 審核未通過，原因：{reason}。');
INSERT INTO notification_template VALUES ('trip_booking_confirmed', '已收到您的 {farm_trip_title} 體驗活動預約，期待您的到來！');
INSERT INTO notification_template VALUES ('trip_booking_new', '體驗活動 {farm_trip_title} 收到一筆新預約！');
INSERT INTO notification_template VALUES ('trip_booking_cancelled', '您已取消體驗活動 {farm_trip_title} 的預約，期待您再次參與。');
INSERT INTO notification_template VALUES ('trip_booking_cancelled_farmer', '消費者已取消一筆 {farm_trip_title} 體驗活動預約，請至體驗活動專區查看。');
INSERT INTO notification_template VALUES ('trip_cancelled', '很抱歉您預約的體驗活動 {farm_trip_title} 已取消，期待您再次參與。');
INSERT INTO notification_template VALUES ('trip_review_invite', '感謝您參加體驗活動 {farm_trip_title}！歡迎留下您的評分與心得，給小農鼓勵。');
INSERT INTO notification_template VALUES ('trip_comment', '體驗活動 {farm_trip_title} 收到一則新評論。');

INSERT INTO notification_template VALUES ('blog_report_pending', '專欄文章 {blog_title} 遭檢舉，目前審核中，請留意後續通知。');
INSERT INTO notification_template VALUES ('blog_report_visible', '專欄文章 {blog_title} 經審核後維持顯示，感謝您的配合。');
INSERT INTO notification_template VALUES ('blog_report_hidden', '專欄文章 {blog_title} 經審核後違反規範已被隱藏，原因：{report_reason}。');
INSERT INTO notification_template VALUES ('blog_comment', '您的文章 {blog_title} 收到一則新留言。');
INSERT INTO notification_template VALUES ('blog_comment_report', '您在 {blog_title} 的留言遭檢舉，目前審核中，請留意後續通知。');
INSERT INTO notification_template VALUES ('blog_comment_report_visible', '您在 {blog_title} 的留言經審核後維持顯示，感謝您的配合。');
INSERT INTO notification_template VALUES ('blog_comment_report_hidden', '您在 {blog_title} 的留言經審核後違反規範已被隱藏，原因：{report_reason}。');

INSERT INTO notification_template VALUES ('account_welcome', '歡迎加入你儂我農 Farmily！快來逛逛新鮮直送的小農好物吧。');
INSERT INTO notification_template VALUES ('account_tier', '您本月的會員等級已更新為 {tier_name}');

INSERT INTO notification_template VALUES ('farmer_review', '小農申請編號 {review_id}，請盡速審核。');
INSERT INTO notification_template VALUES ('blog_report_new', '專欄文章 {blog_id} 遭檢舉，檢舉事由: {report_reason} 請盡速審核。');
INSERT INTO notification_template VALUES ('blog_comment_report_new', '專欄文章 {blog_id} 留言 {comment_id} 遭檢舉，檢舉事由: {report_reason} 請盡速審核。');

INSERT INTO notification_template VALUES ('admin_announcement', '{message}');


-- 7. 通知 - 管理員系統公告
DROP TABLE IF EXISTS notification_announcement;
CREATE TABLE IF NOT EXISTS notification_announcement (
announcement_id INT NOT NULL AUTO_INCREMENT,
admin_id INT NOT NULL,
audience ENUM('user', 'farmer', 'all') NOT NULL,
content VARCHAR(600) NOT NULL, 
created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, 
recipient_count INT,

CONSTRAINT PK_NOTIFANNOUNCEMENT_NBID PRIMARY KEY (announcement_id),
CONSTRAINT FK_NOTIFANNOUNCEMENT_ADMINID FOREIGN KEY (admin_id) REFERENCES admin(admin_id)
)
AUTO_INCREMENT 8001,
ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 參數
INSERT INTO notification_announcement (admin_id, audience, content, recipient_count) VALUES (1, 'user', 'test announcement to user member', 5);


