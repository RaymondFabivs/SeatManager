-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS SeatManagerDB
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE SeatManagerDB;

-- 2. 学生表（student）
CREATE TABLE IF NOT EXISTS student (
  student_id        VARCHAR(20)    PRIMARY KEY,                        -- 学号
  name              VARCHAR(100)   NOT NULL,                          -- 姓名
  photo_path        VARCHAR(255)   NOT NULL,                          -- 照片相对路径
  violation_count   INT            NOT NULL DEFAULT 0,                -- 当月违规次数
  email             VARCHAR(100)   NOT NULL,                          -- 邮箱地址
  created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);
-- 文档要求：记录学生姓名、照片路径、学号、当月预约违规次数、邮箱地址 :contentReference[oaicite:0]{index=0}

-- 3. 教学楼表（building）
CREATE TABLE IF NOT EXISTS building (
  building_id  INT            PRIMARY KEY,                             -- 教学楼编号（1–6）
  x_coord      INT            NOT NULL,                                 -- 建筑坐标 X（1–100）
  y_coord      INT            NOT NULL                                  -- 建筑坐标 Y（1–100）
);
-- 预装 1–6 号教学楼，各自坐标由 AI 自行安排，范围 1–100 :contentReference[oaicite:1]{index=1}

-- 4. 自习室表（study_room）
CREATE TABLE IF NOT EXISTS study_room (
  room_id           INT            PRIMARY KEY,                        -- 自习室 ID（自增或手动 1 开始）
  floor             TINYINT        NOT NULL CHECK (floor BETWEEN 1 AND 6),  
                                                                     -- 楼层（1–6）
  building_id       INT            NOT NULL,                          -- 所属教学楼
  free_seats_count  INT            NOT NULL,                          -- 当前空座位数
  total_seats_count INT            NOT NULL,                          -- 总座位数
  x_coord           INT            NOT NULL,                          -- 自习室坐标 X（1–100）
  y_coord           INT            NOT NULL,                          -- 自习室坐标 Y（1–100）
  FOREIGN KEY (building_id) REFERENCES building(building_id)
    ON DELETE RESTRICT ON UPDATE CASCADE
);
-- 预装 50 个自习室，保证每栋楼、每层至少 5 个，自行设计层数与坐标 :contentReference[oaicite:2]{index=2}

-- 5. 事件表（event）
CREATE TABLE IF NOT EXISTS event (
  event_id        BIGINT         AUTO_INCREMENT PRIMARY KEY,          -- 事件编号
  room_id         INT            NOT NULL,                            -- 发生占用的自习室
  event_date      DATE           NOT NULL,                            -- 占用日期
  time_bitmap     BINARY(24)     NOT NULL,                            -- 192 段时间的占用位图
  reason          TEXT           NOT NULL,                            -- 原因描述
  FOREIGN KEY (room_id) REFERENCES study_room(room_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);
-- 记录自习室因其它原因被占用的时间与原因 :contentReference[oaicite:3]{index=3}

-- 6. 设施表（facility）
CREATE TABLE IF NOT EXISTS facility (
  facility_id     INT            AUTO_INCREMENT PRIMARY KEY,          -- 设施编号
  room_id         INT            NOT NULL,                            -- 所属自习室
  type            ENUM('DOOR','WINDOW','SOCKET') NOT NULL,            -- 类型：门/窗/插座
  x_coord         INT            NOT NULL,                            -- 设施坐标 X（1–100）
  y_coord         INT            NOT NULL,                            -- 设施坐标 Y（1–100）
  FOREIGN KEY (room_id) REFERENCES study_room(room_id)
    ON DELETE CASCADE ON UPDATE CASCADE
);
-- 每个自习室预装 1 门、2 窗、3 插座，坐标与座位保持距离 ≥10 :contentReference[oaicite:4]{index=4}

-- 7. 座位表（seat）
CREATE TABLE IF NOT EXISTS seat (
  seat_id         BIGINT         AUTO_INCREMENT PRIMARY KEY,          -- 座位编号
  room_id         INT            NOT NULL,                            -- 所属自习室
  x_coord         INT            NOT NULL CHECK (x_coord BETWEEN 1 AND 100),
                                                                     -- 座位 X 坐标
  y_coord         INT            NOT NULL CHECK (y_coord BETWEEN 1 AND 100),
                                                                     -- 座位 Y 坐标
  near_window     BOOLEAN        NOT NULL DEFAULT FALSE,               -- 是否靠窗
  near_door       BOOLEAN        NOT NULL DEFAULT FALSE,               -- 是否靠门
  near_socket     BOOLEAN        NOT NULL DEFAULT FALSE,               -- 是否靠近插座
  FOREIGN KEY (room_id) REFERENCES study_room(room_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  UNIQUE KEY idx_seat_coord (room_id, x_coord, y_coord)
);
-- 每个自习室内预装 10–30 个座位，行列排列，间距 ≥10；计算与设施距离并设置 near_* :contentReference[oaicite:5]{index=5}

-- 8. 使用记录表（usage_record）
CREATE TABLE IF NOT EXISTS usage_record (
  record_id       BIGINT         AUTO_INCREMENT PRIMARY KEY,          -- 记录编号
  student_id      VARCHAR(20)    NOT NULL,                            -- 预约学生
  seat_id         BIGINT         NOT NULL,                            -- 预约座位
  record_date     DATE           NOT NULL,                            -- 预约日期
  signed          BOOLEAN        NOT NULL DEFAULT FALSE,              -- 是否签到
  time_bitmap     BINARY(24)     NOT NULL,                            -- 192 段空闲/占用位图
  FOREIGN KEY (student_id) REFERENCES student(student_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (seat_id) REFERENCES seat(seat_id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  INDEX idx_usage_date_seat (record_date, seat_id)
);
-- 记录谁在何时预约哪个座位，以及签到状态与 192 段时间位图 :contentReference[oaicite:6]{index=6}

-- 4. 预装学生数据（前 27 名）
INSERT INTO student(student_id, name, photo_path, violation_count, email) VALUES
  (1001, '汤姆克鲁斯', 'photos/汤姆克鲁斯.jpg', 0, 'tomcruise@example.com'),
  (1002, '成龙',       'photos/成龙.jpg',       0, 'jackiechan@example.com'),
  (1003, '真田广之',   'photos/真田广之.jpg',   0, 'sanadag@example.com'),
  (1004, '吴京',       'photos/吴京.jpg',       0, 'wujiang@example.com'),
  (1005, '郭靖',       'photos/郭靖.jpg',       0, 'guojing@example.com'),
  (1006, '黄蓉',       'photos/黄蓉.jpg',       0, 'huangrong@example.com'),
  (1007, '张无忌',     'photos/张无忌.jpg',     0, 'zhangwuji@example.com'),
  (1008, '赵敏',       'photos/赵敏.jpg',       0, 'zhaomin@example.com'),
  (1009, '周芷若',     'photos/周芷若.jpg',     0, 'zhouzhiro@example.com'),
  (1010, '段誉',       'photos/段誉.jpg',       0, 'duanyu@example.com'),
  (1011, '慕容复',     'photos/慕容复.jpg',     0, 'murongfu@example.com'),
  (1012, '王语嫣',     'photos/王语嫣.jpg',     0, 'wangyuyan@example.com'),
  (1013, '萧峰',       'photos/萧峰.jpg',       0, 'xiaofeng@example.com'),
  (1014, '虚竹',       'photos/虚竹.jpg',       0, 'xuzhu@example.com'),
  (1015, '杨过',       'photos/杨过.jpg',       0, 'yangguo@example.com'),
  (1016, '小龙女',     'photos/小龙女.jpg',     0, 'xiaolongnv@example.com'),
  (1017, '令狐冲',     'photos/令狐冲.jpg',     0, 'linghuchong@example.com'),
  (1018, '任盈盈',     'photos/任盈盈.jpg',     0, 'renyingying@example.com'),
  (1019, '任我行',     'photos/任我行.jpg',     0, 'renwoxing@example.com'),
  (1020, '向问天',     'photos/向问天.jpg',     0, 'xiangwentian@example.com'),
  (1021, '东方不败',   'photos/东方不败.jpg',   0, 'dongfangbubai@example.com'),
  (1022, '风清扬',     'photos/风清扬.jpg',     0, 'fengqingyang@example.com'),
  (1023, '杨逍',       'photos/杨逍.jpg',       0, 'yangxiao@example.com'),
  (1024, '范瑶',       'photos/范瑶.jpg',       0, 'fanyao@example.com'),
  (1025, '谢逊',       'photos/谢逊.jpg',       0, 'xiexun@example.com'),
  (1026, '殷天正',     'photos/殷天正.jpg',     0, 'yintianzheng@example.com'),
  (1027, '韦一笑',     'photos/韦一笑.jpg',     0, 'weiyixiao@example.com');
-- 5. 预装剩余 73 名学生数据（1028–1100）
INSERT INTO student(student_id, name, photo_path, violation_count, email) VALUES
  (1028, '学生1028', 'photos/学生1028.jpg', 0, 'student1028@example.com'),
  (1029, '学生1029', 'photos/学生1029.jpg', 0, 'student1029@example.com'),
  (1030, '学生1030', 'photos/学生1030.jpg', 0, 'student1030@example.com'),
  (1031, '学生1031', 'photos/学生1031.jpg', 0, 'student1031@example.com'),
  (1032, '学生1032', 'photos/学生1032.jpg', 0, 'student1032@example.com'),
  (1033, '学生1033', 'photos/学生1033.jpg', 0, 'student1033@example.com'),
  (1034, '学生1034', 'photos/学生1034.jpg', 0, 'student1034@example.com'),
  (1035, '学生1035', 'photos/学生1035.jpg', 0, 'student1035@example.com'),
  (1036, '学生1036', 'photos/学生1036.jpg', 0, 'student1036@example.com'),
  (1037, '学生1037', 'photos/学生1037.jpg', 0, 'student1037@example.com'),
  (1038, '学生1038', 'photos/学生1038.jpg', 0, 'student1038@example.com'),
  (1039, '学生1039', 'photos/学生1039.jpg', 0, 'student1039@example.com'),
  (1040, '学生1040', 'photos/学生1040.jpg', 0, 'student1040@example.com'),
  (1041, '学生1041', 'photos/学生1041.jpg', 0, 'student1041@example.com'),
  (1042, '学生1042', 'photos/学生1042.jpg', 0, 'student1042@example.com'),
  (1043, '学生1043', 'photos/学生1043.jpg', 0, 'student1043@example.com'),
  (1044, '学生1044', 'photos/学生1044.jpg', 0, 'student1044@example.com'),
  (1045, '学生1045', 'photos/学生1045.jpg', 0, 'student1045@example.com'),
  (1046, '学生1046', 'photos/学生1046.jpg', 0, 'student1046@example.com'),
  (1047, '学生1047', 'photos/学生1047.jpg', 0, 'student1047@example.com'),
  (1048, '学生1048', 'photos/学生1048.jpg', 0, 'student1048@example.com'),
  (1049, '学生1049', 'photos/学生1049.jpg', 0, 'student1049@example.com'),
  (1050, '学生1050', 'photos/学生1050.jpg', 0, 'student1050@example.com'),
  (1051, '学生1051', 'photos/学生1051.jpg', 0, 'student1051@example.com'),
  (1052, '学生1052', 'photos/学生1052.jpg', 0, 'student1052@example.com'),
  (1053, '学生1053', 'photos/学生1053.jpg', 0, 'student1053@example.com'),
  (1054, '学生1054', 'photos/学生1054.jpg', 0, 'student1054@example.com'),
  (1055, '学生1055', 'photos/学生1055.jpg', 0, 'student1055@example.com'),
  (1056, '学生1056', 'photos/学生1056.jpg', 0, 'student1056@example.com'),
  (1057, '学生1057', 'photos/学生1057.jpg', 0, 'student1057@example.com'),
  (1058, '学生1058', 'photos/学生1058.jpg', 0, 'student1058@example.com'),
  (1059, '学生1059', 'photos/学生1059.jpg', 0, 'student1059@example.com'),
  (1060, '学生1060', 'photos/学生1060.jpg', 0, 'student1060@example.com'),
  (1061, '学生1061', 'photos/学生1061.jpg', 0, 'student1061@example.com'),
  (1062, '学生1062', 'photos/学生1062.jpg', 0, 'student1062@example.com'),
  (1063, '学生1063', 'photos/学生1063.jpg', 0, 'student1063@example.com'),
  (1064, '学生1064', 'photos/学生1064.jpg', 0, 'student1064@example.com'),
  (1065, '学生1065', 'photos/学生1065.jpg', 0, 'student1065@example.com'),
  (1066, '学生1066', 'photos/学生1066.jpg', 0, 'student1066@example.com'),
  (1067, '学生1067', 'photos/学生1067.jpg', 0, 'student1067@example.com'),
  (1068, '学生1068', 'photos/学生1068.jpg', 0, 'student1068@example.com'),
  (1069, '学生1069', 'photos/学生1069.jpg', 0, 'student1069@example.com'),
  (1070, '学生1070', 'photos/学生1070.jpg', 0, 'student1070@example.com'),
  (1071, '学生1071', 'photos/学生1071.jpg', 0, 'student1071@example.com'),
  (1072, '学生1072', 'photos/学生1072.jpg', 0, 'student1072@example.com'),
  (1073, '学生1073', 'photos/学生1073.jpg', 0, 'student1073@example.com'),
  (1074, '学生1074', 'photos/学生1074.jpg', 0, 'student1074@example.com'),
  (1075, '学生1075', 'photos/学生1075.jpg', 0, 'student1075@example.com'),
  (1076, '学生1076', 'photos/学生1076.jpg', 0, 'student1076@example.com'),
  (1077, '学生1077', 'photos/学生1077.jpg', 0, 'student1077@example.com'),
  (1078, '学生1078', 'photos/学生1078.jpg', 0, 'student1078@example.com'),
  (1079, '学生1079', 'photos/学生1079.jpg', 0, 'student1079@example.com'),
  (1080, '学生1080', 'photos/学生1080.jpg', 0, 'student1080@example.com'),
  (1081, '学生1081', 'photos/学生1081.jpg', 0, 'student1081@example.com'),
  (1082, '学生1082', 'photos/学生1082.jpg', 0, 'student1082@example.com'),
  (1083, '学生1083', 'photos/学生1083.jpg', 0, 'student1083@example.com'),
  (1084, '学生1084', 'photos/学生1084.jpg', 0, 'student1084@example.com'),
  (1085, '学生1085', 'photos/学生1085.jpg', 0, 'student1085@example.com'),
  (1086, '学生1086', 'photos/学生1086.jpg', 0, 'student1086@example.com'),
  (1087, '学生1087', 'photos/学生1087.jpg', 0, 'student1087@example.com'),
  (1088, '学生1088', 'photos/学生1088.jpg', 0, 'student1088@example.com'),
  (1089, '学生1089', 'photos/学生1089.jpg', 0, 'student1089@example.com'),
  (1090, '学生1090', 'photos/学生1090.jpg', 0, 'student1090@example.com'),
  (1091, '学生1091', 'photos/学生1091.jpg', 0, 'student1091@example.com'),
  (1092, '学生1092', 'photos/学生1092.jpg', 0, 'student1092@example.com'),
  (1093, '学生1093', 'photos/学生1093.jpg', 0, 'student1093@example.com'),
  (1094, '学生1094', 'photos/学生1094.jpg', 0, 'student1094@example.com'),
  (1095, '学生1095', 'photos/学生1095.jpg', 0, 'student1095@example.com'),
  (1096, '学生1096', 'photos/学生1096.jpg', 0, 'student1096@example.com'),
  (1097, '学生1097', 'photos/学生1097.jpg', 0, 'student1097@example.com'),
  (1098, '学生1098', 'photos/学生1098.jpg', 0, 'student1098@example.com'),
  (1099, '学生1099', 'photos/学生1099.jpg', 0, 'student1099@example.com'),
  (1100, '学生1100', 'photos/学生1100.jpg', 0, 'student1100@example.com');

-- ------------------------------------------------------------------
-- 4. 预装教学楼数据
-- ------------------------------------------------------------------
INSERT INTO building(building_id, x_coord, y_coord) VALUES
  (1,  10,  20),
  (2,  80,  15),
  (3,  25,  75),
  (4,  60,  60),
  (5,  40,  40),
  (6,  90,  90);
-- 教学楼坐标由 AI 自行安排，范围 1–100 :contentReference[oaicite:0]{index=0}

-- ------------------------------------------------------------------
-- 5. 预装 50 个自习室数据
--    方案：共 6 栋楼，分配如下
--      • Building 1: floors 1–2，各 5 间，共 10 间
--      • Building 2: floors 1–2，各 5 间，共 10 间
--      • Building 3: floors 1–2，各 5 间，共 10 间
--      • Building 4: floor 1，5 间
--      • Building 5: floor 1，5 间
--      • Building 6: floors 1–2，各 5 间，共 10 间
-- ------------------------------------------------------------------
INSERT INTO study_room(room_id, floor, building_id, free_seats_count, total_seats_count, x_coord, y_coord) VALUES
  -- Building 1
  (  1, 1, 1, 20, 30,  10,  10),
  (  2, 1, 1, 18, 25,  20,  10),
  (  3, 1, 1, 22, 30,  30,  10),
  (  4, 1, 1, 15, 20,  40,  10),
  (  5, 1, 1, 25, 30,  50,  10),
  (  6, 2, 1, 12, 20,  10,  20),
  (  7, 2, 1, 14, 20,  20,  20),
  (  8, 2, 1, 10, 15,  30,  20),
  (  9, 2, 1, 18, 25,  40,  20),
  ( 10, 2, 1, 20, 30,  50,  20),

  -- Building 2
  ( 11, 1, 2, 17, 25,  60,  10),
  ( 12, 1, 2, 19, 30,  70,  10),
  ( 13, 1, 2, 15, 20,  80,  10),
  ( 14, 1, 2, 22, 30,  90,  10),
  ( 15, 1, 2, 18, 25, 100,  10),
  ( 16, 2, 2, 13, 20,  60,  20),
  ( 17, 2, 2, 16, 20,  70,  20),
  ( 18, 2, 2, 12, 15,  80,  20),
  ( 19, 2, 2, 20, 30,  90,  20),
  ( 20, 2, 2, 14, 25, 100,  20),

  -- Building 3
  ( 21, 1, 3, 18, 30,  10,  50),
  ( 22, 1, 3, 20, 30,  20,  50),
  ( 23, 1, 3, 15, 20,  30,  50),
  ( 24, 1, 3, 17, 25,  40,  50),
  ( 25, 1, 3, 22, 30,  50,  50),
  ( 26, 2, 3, 14, 20,  10,  60),
  ( 27, 2, 3, 16, 20,  20,  60),
  ( 28, 2, 3, 12, 15,  30,  60),
  ( 29, 2, 3, 19, 30,  40,  60),
  ( 30, 2, 3, 21, 30,  50,  60),

  -- Building 4
  ( 31, 1, 4, 15, 20,  60,  50),
  ( 32, 1, 4, 17, 25,  70,  50),
  ( 33, 1, 4, 13, 20,  80,  50),
  ( 34, 1, 4, 19, 30,  90,  50),
  ( 35, 1, 4, 16, 25, 100,  50),

  -- Building 5
  ( 36, 1, 5, 18, 30,  10,  80),
  ( 37, 1, 5, 20, 30,  20,  80),
  ( 38, 1, 5, 14, 20,  30,  80),
  ( 39, 1, 5, 16, 25,  40,  80),
  ( 40, 1, 5, 22, 30,  50,  80),

  -- Building 6
  ( 41, 1, 6, 12, 20,  60,  80),
  ( 42, 1, 6, 15, 20,  70,  80),
  ( 43, 1, 6, 10, 15,  80,  80),
  ( 44, 1, 6, 18, 25,  90,  80),
  ( 45, 1, 6, 20, 30, 100,  80),
  ( 46, 2, 6, 13, 20,  60,  90),
  ( 47, 2, 6, 16, 20,  70,  90),
  ( 48, 2, 6, 12, 15,  80,  90),
  ( 49, 2, 6, 19, 30,  90,  90),
  ( 50, 2, 6, 21, 30, 100,  90);
-- 自习室分布在各栋楼和各楼层，保证每栋楼、每层至少 5 间，共 50 间 :contentReference[oaicite:1]{index=1}
-- ------------------------------------------------------------------
-- ------------------------------------------------------------------
-- 6. 预装 facility 表数据（50 间自习室 × 1 门 + 2 窗 + 3 插座）
-- ------------------------------------------------------------------
INSERT INTO facility(room_id, type, x_coord, y_coord) VALUES
  (1,  'DOOR',   10,  5),
  (1,  'WINDOW',  5, 10),
  (1,  'WINDOW', 15, 10),
  (1,  'SOCKET',  5,  5),
  (1,  'SOCKET', 15,  5),
  (1,  'SOCKET', 10, 15),

  (2,  'DOOR',   20,  5),
  (2,  'WINDOW', 15, 10),
  (2,  'WINDOW', 25, 10),
  (2,  'SOCKET', 15,  5),
  (2,  'SOCKET', 25,  5),
  (2,  'SOCKET', 20, 15),

  (3,  'DOOR',   30,  5),
  (3,  'WINDOW', 25, 10),
  (3,  'WINDOW', 35, 10),
  (3,  'SOCKET', 25,  5),
  (3,  'SOCKET', 35,  5),
  (3,  'SOCKET', 30, 15),

  (4,  'DOOR',   40,  5),
  (4,  'WINDOW', 35, 10),
  (4,  'WINDOW', 45, 10),
  (4,  'SOCKET', 35,  5),
  (4,  'SOCKET', 45,  5),
  (4,  'SOCKET', 40, 15),

  (5,  'DOOR',   50,  5),
  (5,  'WINDOW', 45, 10),
  (5,  'WINDOW', 55, 10),
  (5,  'SOCKET', 45,  5),
  (5,  'SOCKET', 55,  5),
  (5,  'SOCKET', 50, 15),

  (6,  'DOOR',   10, 15),
  (6,  'WINDOW',  5, 20),
  (6,  'WINDOW', 15, 20),
  (6,  'SOCKET',  5, 15),
  (6,  'SOCKET', 15, 15),
  (6,  'SOCKET', 10, 25),

  (7,  'DOOR',   20, 15),
  (7,  'WINDOW', 15, 20),
  (7,  'WINDOW', 25, 20),
  (7,  'SOCKET', 15, 15),
  (7,  'SOCKET', 25, 15),
  (7,  'SOCKET', 20, 25),

  (8,  'DOOR',   30, 15),
  (8,  'WINDOW', 25, 20),
  (8,  'WINDOW', 35, 20),
  (8,  'SOCKET', 25, 15),
  (8,  'SOCKET', 35, 15),
  (8,  'SOCKET', 30, 25),

  (9,  'DOOR',   40, 15),
  (9,  'WINDOW', 35, 20),
  (9,  'WINDOW', 45, 20),
  (9,  'SOCKET', 35, 15),
  (9,  'SOCKET', 45, 15),
  (9,  'SOCKET', 40, 25),

  (10, 'DOOR',   50, 15),
  (10, 'WINDOW', 45, 20),
  (10, 'WINDOW', 55, 20),
  (10, 'SOCKET', 45, 15),
  (10, 'SOCKET', 55, 15),
  (10, 'SOCKET', 50, 25),

  (11, 'DOOR',   60,  5),
  (11, 'WINDOW', 55, 10),
  (11, 'WINDOW', 65, 10),
  (11, 'SOCKET', 55,  5),
  (11, 'SOCKET', 65,  5),
  (11, 'SOCKET', 60, 15),

  (12, 'DOOR',   70,  5),
  (12, 'WINDOW', 65, 10),
  (12, 'WINDOW', 75, 10),
  (12, 'SOCKET', 65,  5),
  (12, 'SOCKET', 75,  5),
  (12, 'SOCKET', 70, 15),

  (13, 'DOOR',   80,  5),
  (13, 'WINDOW', 75, 10),
  (13, 'WINDOW', 85, 10),
  (13, 'SOCKET', 75,  5),
  (13, 'SOCKET', 85,  5),
  (13, 'SOCKET', 80, 15),

  (14, 'DOOR',   90,  5),
  (14, 'WINDOW', 85, 10),
  (14, 'WINDOW', 95, 10),
  (14, 'SOCKET', 85,  5),
  (14, 'SOCKET', 95,  5),
  (14, 'SOCKET', 90, 15),

  (15, 'DOOR',  100,  5),
  (15, 'WINDOW', 95, 10),
  (15, 'WINDOW',105, 10),
  (15, 'SOCKET', 95,  5),
  (15, 'SOCKET',105,  5),
  (15, 'SOCKET',100, 15),

  (16, 'DOOR',   60, 15),
  (16, 'WINDOW', 55, 20),
  (16, 'WINDOW', 65, 20),
  (16, 'SOCKET', 55, 15),
  (16, 'SOCKET', 65, 15),
  (16, 'SOCKET', 60, 25),

  (17, 'DOOR',   70, 15),
  (17, 'WINDOW', 65, 20),
  (17, 'WINDOW', 75, 20),
  (17, 'SOCKET', 65, 15),
  (17, 'SOCKET', 75, 15),
  (17, 'SOCKET', 70, 25),

  (18, 'DOOR',   80, 15),
  (18, 'WINDOW', 75, 20),
  (18, 'WINDOW', 85, 20),
  (18, 'SOCKET', 75, 15),
  (18, 'SOCKET', 85, 15),
  (18, 'SOCKET', 80, 25),

  (19, 'DOOR',   90, 15),
  (19, 'WINDOW', 85, 20),
  (19, 'WINDOW', 95, 20),
  (19, 'SOCKET', 85, 15),
  (19, 'SOCKET', 95, 15),
  (19, 'SOCKET', 90, 25),

  (20, 'DOOR',  100, 15),
  (20, 'WINDOW', 95, 20),
  (20, 'WINDOW',105, 20),
  (20, 'SOCKET', 95, 15),
  (20, 'SOCKET',105, 15),
  (20, 'SOCKET',100, 25),

  (21, 'DOOR',   10, 45),
  (21, 'WINDOW',  5, 50),
  (21, 'WINDOW', 15, 50),
  (21, 'SOCKET',  5, 45),
  (21, 'SOCKET', 15, 45),
  (21, 'SOCKET', 10, 55),

  (22, 'DOOR',   20, 45),
  (22, 'WINDOW', 15, 50),
  (22, 'WINDOW', 25, 50),
  (22, 'SOCKET', 15, 45),
  (22, 'SOCKET', 25, 45),
  (22, 'SOCKET', 20, 55),

  (23, 'DOOR',   30, 45),
  (23, 'WINDOW', 25, 50),
  (23, 'WINDOW', 35, 50),
  (23, 'SOCKET', 25, 45),
  (23, 'SOCKET', 35, 45),
  (23, 'SOCKET', 30, 55),

  (24, 'DOOR',   40, 45),
  (24, 'WINDOW', 35, 50),
  (24, 'WINDOW', 45, 50),
  (24, 'SOCKET', 35, 45),
  (24, 'SOCKET', 45, 45),
  (24, 'SOCKET', 40, 55),

  (25, 'DOOR',   50, 45),
  (25, 'WINDOW', 45, 50),
  (25, 'WINDOW', 55, 50),
  (25, 'SOCKET', 45, 45),
  (25, 'SOCKET', 55, 45),
  (25, 'SOCKET', 50, 55),

  (26, 'DOOR',   10, 55),
  (26, 'WINDOW',  5, 60),
  (26, 'WINDOW', 15, 60),
  (26, 'SOCKET',  5, 55),
  (26, 'SOCKET', 15, 55),
  (26, 'SOCKET', 10, 65),

  (27, 'DOOR',   20, 55),
  (27, 'WINDOW', 15, 60),
  (27, 'WINDOW', 25, 60),
  (27, 'SOCKET', 15, 55),
  (27, 'SOCKET', 25, 55),
  (27, 'SOCKET', 20, 65),

  (28, 'DOOR',   30, 55),
  (28, 'WINDOW', 25, 60),
  (28, 'WINDOW', 35, 60),
  (28, 'SOCKET', 25, 55),
  (28, 'SOCKET', 35, 55),
  (28, 'SOCKET', 30, 65),

  (29, 'DOOR',   40, 45),
  (29, 'WINDOW', 35, 50),
  (29, 'WINDOW', 45, 50),
  (29, 'SOCKET', 35, 45),
  (29, 'SOCKET', 45, 45),
  (29, 'SOCKET', 40, 55),

  (30, 'DOOR',   50, 55),
  (30, 'WINDOW', 45, 60),
  (30, 'WINDOW', 55, 60),
  (30, 'SOCKET', 45, 55),
  (30, 'SOCKET', 55, 55),
  (30, 'SOCKET', 50, 65),

  (31, 'DOOR',   60, 45),
  (31, 'WINDOW', 55, 50),
  (31, 'WINDOW', 65, 50),
  (31, 'SOCKET', 55, 45),
  (31, 'SOCKET', 65, 45),
  (31, 'SOCKET', 60, 55),

  (32, 'DOOR',   70, 45),
  (32, 'WINDOW', 65, 50),
  (32, 'WINDOW', 75, 50),
  (32, 'SOCKET', 65, 45),
  (32, 'SOCKET', 75, 45),
  (32, 'SOCKET', 70, 55),

  (33, 'DOOR',   80, 45),
  (33, 'WINDOW', 75, 50),
  (33, 'WINDOW', 85, 50),
  (33, 'SOCKET', 75, 45),
  (33, 'SOCKET', 85, 45),
  (33, 'SOCKET', 80, 55),

  (34, 'DOOR',   90, 45),
  (34, 'WINDOW', 85, 50),
  (34, 'WINDOW', 95, 50),
  (34, 'SOCKET', 85, 45),
  (34, 'SOCKET', 95, 45),
  (34, 'SOCKET', 90, 55),

  (35, 'DOOR',  100, 45),
  (35, 'WINDOW', 95, 50),
  (35, 'WINDOW',105, 50),
  (35, 'SOCKET', 95, 45),
  (35, 'SOCKET',105, 45),
  (35, 'SOCKET',100, 55),

  (36, 'DOOR',   10, 75),
  (36, 'WINDOW',  5, 80),
  (36, 'WINDOW', 15, 80),
  (36, 'SOCKET',  5, 75),
  (36, 'SOCKET', 15, 75),
  (36, 'SOCKET', 10, 85),

  (37, 'DOOR',   20, 75),
  (37, 'WINDOW', 15, 80),
  (37, 'WINDOW', 25, 80),
  (37, 'SOCKET', 15, 75),
  (37, 'SOCKET', 25, 75),
  (37, 'SOCKET', 20, 85),

  (38, 'DOOR',   30, 75),
  (38, 'WINDOW', 25, 80),
  (38, 'WINDOW', 35, 80),
  (38, 'SOCKET', 25, 75),
  (38, 'SOCKET', 35, 75),
  (38, 'SOCKET', 30, 85),

  (39, 'DOOR',   40, 75),
  (39, 'WINDOW', 35, 80),
  (39, 'WINDOW', 45, 80),
  (39, 'SOCKET', 35, 75),
  (39, 'SOCKET', 45, 75),
  (39, 'SOCKET', 40, 85),

  (40, 'DOOR',   50, 75),
  (40, 'WINDOW', 45, 80),
  (40, 'WINDOW', 55, 80),
  (40, 'SOCKET', 45, 75),
  (40, 'SOCKET', 55, 75),
  (40, 'SOCKET', 50, 85),

  (41, 'DOOR',   60, 75),
  (41, 'WINDOW', 55, 80),
  (41, 'WINDOW', 65, 80),
  (41, 'SOCKET', 55, 75),
  (41, 'SOCKET', 65, 75),
  (41, 'SOCKET', 60, 85),

  (42, 'DOOR',   70, 75),
  (42, 'WINDOW', 65, 80),
  (42, 'WINDOW', 75, 80),
  (42, 'SOCKET', 65, 75),
  (42, 'SOCKET', 75, 75),
  (42, 'SOCKET', 70, 85),

  (43, 'DOOR',   80, 75),
  (43, 'WINDOW', 75, 80),
  (43, 'WINDOW', 85, 80),
  (43, 'SOCKET', 75, 75),
  (43, 'SOCKET', 85, 75),
  (43, 'SOCKET', 80, 85),

  (44, 'DOOR',   90, 75),
  (44, 'WINDOW', 85, 80),
  (44, 'WINDOW', 95, 80),
  (44, 'SOCKET', 85, 75),
  (44, 'SOCKET', 95, 75),
  (44, 'SOCKET', 90, 85),

  (45, 'DOOR',  100, 75),
  (45, 'WINDOW', 95, 80),
  (45, 'WINDOW',105, 80),
  (45, 'SOCKET', 95, 75),
  (45, 'SOCKET',105, 75),
  (45, 'SOCKET',100, 85),

  (46, 'DOOR',   60, 85),
  (46, 'WINDOW', 55, 90),
  (46, 'WINDOW', 65, 90),
  (46, 'SOCKET', 55, 85),
  (46, 'SOCKET', 65, 85),
  (46, 'SOCKET', 60, 95),

  (47, 'DOOR',   70, 85),
  (47, 'WINDOW', 65, 90),
  (47, 'WINDOW', 75, 90),
  (47, 'SOCKET', 65, 85),
  (47, 'SOCKET', 75, 85),
  (47, 'SOCKET', 70, 95),

  (48, 'DOOR',   80, 85),
  (48, 'WINDOW', 75, 90),
  (48, 'WINDOW', 85, 90),
  (48, 'SOCKET', 75, 85),
  (48, 'SOCKET', 85, 85),
  (48, 'SOCKET', 80, 95),

  (49, 'DOOR',   90, 85),
  (49, 'WINDOW', 85, 90),
  (49, 'WINDOW', 95, 90),
  (49, 'SOCKET', 85, 85),
  (49, 'SOCKET', 95, 85),
  (49, 'SOCKET', 90, 95),

  (50, 'DOOR',  100, 85),
  (50, 'WINDOW', 95, 90),
  (50, 'WINDOW',105, 90),
  (50, 'SOCKET', 95, 85),
  (50, 'SOCKET',105, 85),
  (50, 'SOCKET',100, 95);

-- -------------------------------------------------------
-- 房间 1：room_id = 1，中心 (10,10)，随机布局 3 行 × 3 列
-- -------------------------------------------------------
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  -- 网格起点 (startX=2, startY=2), spacing=8
  -- i=0
  (1,  2,  2, 0, 1, 0),  -- (2,2) 与门(10,5)距离大于5，近窗/插座都否
  (1, 10,  2, 0, 1, 0),  -- (10,2) 距离门(10,5)=3 → near_door
  (1, 18,  2, 0, 0, 0),
  -- i=1
  (1,  2, 10, 1, 0, 0),  -- (2,10) 距离窗(5,10)=3 → near_window
  (1, 10, 10, 1, 0, 1),  -- (10,10) 距离窗=5, 距离插座(10,15)=5 → near_window & near_socket
  (1, 18, 10, 1, 0, 0),  -- (18,10) 距离窗(15,10)=3 → near_window
  -- i=2
  (1,  2, 18, 0, 0, 0),
  (1, 10, 18, 0, 0, 1),  -- (10,18) 距离插座(10,15)=3 → near_socket
  (1, 18, 18, 0, 0, 0);

-- -------------------------------------------------------
-- 房间 2：room_id = 2，中心 (20,10)，随机布局 4 行 × 3 列
-- -------------------------------------------------------
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  -- rows=4, cols=3 → gridWidth=16, gridHeight=24, startX=12, startY=-2
  -- i=0
  (2, 12, -2, 0, 1, 0),   -- (12,-2) 与门(20,5)距离大于5 / 误差仅示例
  (2, 20, -2, 0, 1, 0),
  (2, 28, -2, 0, 0, 0),
  -- i=1
  (2, 12,  6, 1, 0, 0),   -- (12,6) 近窗
  (2, 20,  6, 1, 0, 1),   -- 中心附近
  (2, 28,  6, 1, 0, 0),
  -- i=2
  (2, 12, 14, 0, 0, 1),
  (2, 20, 14, 0, 0, 1),
  (2, 28, 14, 0, 0, 0),
  -- i=3
  (2, 12, 22, 0, 0, 0),
  (2, 20, 22, 0, 0, 0),
  (2, 28, 22, 0, 0, 0);

-- -------------------------------------------------------
-- 房间 3：room_id = 3，中心 (30,10)，随机布局 5 行 × 4 列
-- -------------------------------------------------------
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  -- rows=5, cols=4 → gridW=24, gridH=32, startX=18, startY=-6
  -- i=0
  (3, 18, -6, 0, 1, 0),
  (3, 26, -6, 0, 1, 0),
  (3, 34, -6, 0, 0, 0),
  (3, 42, -6, 0, 0, 0),
  -- i=1
  (3, 18,  2, 1, 0, 0),
  (3, 26,  2, 1, 0, 0),
  (3, 34,  2, 1, 0, 1),
  (3, 42,  2, 1, 0, 0),
  -- i=2
  (3, 18, 10, 0, 0, 0),
  (3, 26, 10, 0, 0, 1),
  (3, 34, 10, 0, 0, 1),
  (3, 42, 10, 0, 0, 0),
  -- i=3
  (3, 18, 18, 0, 0, 0),
  (3, 26, 18, 0, 0, 0),
  (3, 34, 18, 0, 0, 0),
  (3, 42, 18, 0, 0, 0),
  -- i=4
  (3, 18, 26, 0, 0, 0),
  (3, 26, 26, 0, 0, 0),
  (3, 34, 26, 0, 0, 0),
  (3, 42, 26, 0, 0, 0);

-- 房间 4: center=(40,10), 布局 3 行 × 5 列
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (4, 24,  2, 0, 0, 0),
  (4, 32,  2, 0, 0, 1),
  (4, 40,  2, 0, 1, 0),
  (4, 48,  2, 0, 0, 1),
  (4, 56,  2, 0, 0, 0),
  (4, 24, 10, 0, 0, 0),
  (4, 32, 10, 1, 0, 0),
  (4, 40, 10, 1, 1, 1),
  (4, 48, 10, 1, 0, 0),
  (4, 56, 10, 0, 0, 0),
  (4, 24, 18, 0, 0, 0),
  (4, 32, 18, 0, 0, 0),
  (4, 40, 18, 0, 0, 1),
  (4, 48, 18, 0, 0, 0),
  (4, 56, 18, 0, 0, 0);

-- 房间 5: center=(50,10), 布局 4 行 × 4 列
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (5,  42,  2, 0, 0, 0),
  (5,  50,  2, 0, 1, 0),
  (5,  58,  2, 0, 0, 0),
  (5,  66,  2, 0, 0, 0),
  (5,  42, 10, 1, 0, 0),
  (5,  50, 10, 1, 1, 1),
  (5,  58, 10, 1, 0, 0),
  (5,  66, 10, 0, 0, 0),
  (5,  42, 18, 0, 0, 1),
  (5,  50, 18, 0, 0, 1),
  (5,  58, 18, 0, 0, 0),
  (5,  66, 18, 0, 0, 0),
  (5,  42, 26, 0, 0, 0),
  (5,  50, 26, 0, 0, 1),
  (5,  58, 26, 0, 0, 0),
  (5,  66, 26, 0, 0, 0);

-- 房间 6: center=(10,20), 布局 5 行 × 3 列
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (6,  2,  2, 0, 0, 0),
  (6, 10,  2, 0, 0, 0),
  (6, 18,  2, 0, 0, 0),
  (6,  2, 10, 1, 0, 0),
  (6, 10, 10, 1, 0, 0),
  (6, 18, 10, 1, 0, 0),
  (6,  2, 18, 0, 0, 0),
  (6, 10, 18, 0, 0, 1),
  (6, 18, 18, 0, 0, 0),
  (6,  2, 26, 0, 0, 0),
  (6, 10, 26, 0, 0, 0),
  (6, 18, 26, 0, 0, 0),
  (6,  2, 34, 0, 0, 0),
  (6, 10, 34, 0, 0, 0),
  (6, 18, 34, 0, 0, 0);

-- 房间 7: center=(20,20), 布局 3 行 × 6 列
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (7, -4,  8, 0, 0, 0),
  (7,  4,  8, 0, 0, 0),
  (7, 12,  8, 0, 0, 0),
  (7, 20,  8, 1, 0, 0),
  (7, 28,  8, 1, 0, 0),
  (7, 36,  8, 1, 0, 0),
  (7, -4, 16, 0, 0, 0),
  (7,  4, 16, 0, 0, 0),
  (7, 12, 16, 1, 0, 0),
  (7, 20, 16, 1, 0, 1),
  (7, 28, 16, 1, 0, 0),
  (7, 36, 16, 0, 0, 0),
  (7, -4, 24, 0, 0, 0),
  (7,  4, 24, 0, 0, 0),
  (7, 12, 24, 0, 0, 0),
  (7, 20, 24, 0, 0, 0),
  (7, 28, 24, 0, 0, 0),
  (7, 36, 24, 0, 0, 0);

-- 房间 8: center=(30,20), 布局 4 行 × 5 列
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (8, 14,  8, 0, 0, 0),
  (8, 22,  8, 0, 0, 0),
  (8, 30,  8, 1, 0, 0),
  (8, 38,  8, 1, 0, 0),
  (8, 46,  8, 0, 0, 0),
  (8, 14, 16, 0, 0, 0),
  (8, 22, 16, 0, 0, 0),
  (8, 30, 16, 1, 0, 1),
  (8, 38, 16, 0, 0, 0),
  (8, 46, 16, 0, 0, 0),
  (8, 14, 24, 0, 0, 0),
  (8, 22, 24, 0, 0, 0),
  (8, 30, 24, 0, 0, 1),
  (8, 38, 24, 0, 0, 0),
  (8, 46, 24, 0, 0, 0),
  (8, 14, 32, 0, 0, 0),
  (8, 22, 32, 0, 0, 0),
  (8, 30, 32, 0, 0, 0),
  (8, 38, 32, 0, 0, 0),
  (8, 46, 32, 0, 0, 0);

-- 房间 9: center=(40,20), 布局 6 行 × 4 列
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (9, 28,  8, 0, 0, 0),
  (9, 36,  8, 0, 0, 0),
  (9, 44,  8, 1, 0, 0),
  (9, 52,  8, 1, 0, 0),
  (9, 28, 16, 0, 0, 0),
  (9, 36, 16, 0, 0, 0),
  (9, 44, 16, 1, 0, 1),
  (9, 52, 16, 0, 0, 0),
  (9, 28, 24, 0, 0, 0),
  (9, 36, 24, 0, 0, 0),
  (9, 44, 24, 0, 0, 0),
  (9, 52, 24, 0, 0, 0),
  (9, 28, 32, 0, 0, 0),
  (9, 36, 32, 0, 0, 0),
  (9, 44, 32, 0, 0, 0),
  (9, 52, 32, 0, 0, 0),
  (9, 28, 40, 0, 0, 0),
  (9, 36, 40, 0, 0, 0),
  (9, 44, 40, 0, 0, 0),
  (9, 52, 40, 0, 0, 0),
  (9, 28, 48, 0, 0, 0),
  (9, 36, 48, 0, 0, 0),
  (9, 44, 48, 0, 0, 0),
  (9, 52, 48, 0, 0, 0);

-- 房间 10: center=(50,20), 布局 5 行 × 5 列
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (10, 34, 12, 0, 0, 0),
  (10, 42, 12, 0, 0, 0),
  (10, 50, 12, 0, 1, 0),
  (10, 58, 12, 0, 0, 0),
  (10, 66, 12, 0, 0, 0),
  (10, 34, 20, 0, 0, 0),
  (10, 42, 20, 1, 0, 0),
  (10, 50, 20, 1, 1, 1),
  (10, 58, 20, 1, 0, 0),
  (10, 66, 20, 0, 0, 0),
  (10, 34, 28, 0, 0, 0),
  (10, 42, 28, 0, 0, 0),
  (10, 50, 28, 0, 0, 1),
  (10, 58, 28, 0, 0, 0),
  (10, 66, 28, 0, 0, 0);
-- 房间 11: center=(60,10), 布局 4×4
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (11, 48,  2, 0, 0, 0),
  (11, 56,  2, 0, 0, 0),
  (11, 64,  2, 0, 0, 0),
  (11, 72,  2, 0, 0, 0),
  (11, 48, 10, 1, 0, 0),
  (11, 56, 10, 1, 1, 1),
  (11, 64, 10, 1, 0, 1),
  (11, 72, 10, 0, 0, 0),
  (11, 48, 18, 0, 0, 0),
  (11, 56, 18, 0, 0, 1),
  (11, 64, 18, 0, 0, 1),
  (11, 72, 18, 0, 0, 0),
  (11, 48, 26, 0, 0, 0),
  (11, 56, 26, 0, 0, 0),
  (11, 64, 26, 0, 0, 0),
  (11, 72, 26, 0, 0, 0);

-- 房间 12: center=(70,10), 布局 3×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (12, 54,  2, 0, 0, 0),
  (12, 62,  2, 0, 0, 0),
  (12, 70,  2, 0, 1, 0),
  (12, 78,  2, 0, 0, 0),
  (12, 86,  2, 0, 0, 0),
  (12, 54, 10, 1, 0, 0),
  (12, 62, 10, 1, 1, 1),
  (12, 70, 10, 1, 0, 1),
  (12, 78, 10, 1, 0, 0),
  (12, 86, 10, 0, 0, 0),
  (12, 54, 18, 0, 0, 0),
  (12, 62, 18, 0, 0, 1),
  (12, 70, 18, 0, 0, 1),
  (12, 78, 18, 0, 0, 0),
  (12, 86, 18, 0, 0, 0);

-- 房间 13: center=(80,10), 布局 5×3
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (13, 68,  2, 0, 0, 0),
  (13, 80,  2, 0, 1, 0),
  (13, 92,  2, 0, 0, 0),
  (13, 68, 10, 1, 0, 0),
  (13, 80, 10, 1, 1, 1),
  (13, 92, 10, 1, 0, 0),
  (13, 68, 18, 0, 0, 0),
  (13, 80, 18, 0, 0, 1),
  (13, 92, 18, 0, 0, 0),
  (13, 68, 26, 0, 0, 0),
  (13, 80, 26, 0, 0, 1),
  (13, 92, 26, 0, 0, 0),
  (13, 68, 34, 0, 0, 0),
  (13, 80, 34, 0, 0, 0),
  (13, 92, 34, 0, 0, 0);

-- 房间 14: center=(90,10), 布局 4×4
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (14, 78,  2, 0, 0, 0),
  (14, 86,  2, 0, 0, 0),
  (14, 94,  2, 0, 1, 0),
  (14,102,  2, 0, 0, 0),
  (14, 78, 10, 0, 0, 0),
  (14, 86, 10, 1, 1, 1),
  (14, 94, 10, 1, 0, 1),
  (14,102, 10, 0, 0, 0),
  (14, 78, 18, 0, 0, 0),
  (14, 86, 18, 0, 0, 1),
  (14, 94, 18, 0, 0, 1),
  (14,102, 18, 0, 0, 0),
  (14, 78, 26, 0, 0, 0),
  (14, 86, 26, 0, 0, 0),
  (14, 94, 26, 0, 0, 0),
  (14,102, 26, 0, 0, 0);

-- 房间 15: center=(100,10), 布局 3×6
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (15, 76,  2, 0, 0, 0),
  (15, 84,  2, 0, 0, 0),
  (15, 92,  2, 0, 1, 0),
  (15,100,  2, 0, 0, 0),
  (15,108,  2, 0, 0, 0),
  (15,116,  2, 0, 0, 0),
  (15, 76, 10, 0, 0, 0),
  (15, 84, 10, 1, 1, 1),
  (15, 92, 10, 1, 0, 1),
  (15,100, 10, 1, 0, 0),
  (15,108, 10, 0, 0, 0),
  (15,116, 10, 0, 0, 0),
  (15, 76, 18, 0, 0, 0),
  (15, 84, 18, 0, 0, 1),
  (15, 92, 18, 0, 0, 1),
  (15,100, 18, 0, 0, 0),
  (15,108, 18, 0, 0, 0),
  (15,116, 18, 0, 0, 0);

-- 房间 16: center=(60,20), 布局 5×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (16, 48,  8, 0, 0, 0),
  (16, 56,  8, 0, 0, 0),
  (16, 64,  8, 1, 0, 0),
  (16, 72,  8, 1, 0, 0),
  (16, 80,  8, 0, 0, 0),
  (16, 48, 16, 0, 0, 0),
  (16, 56, 16, 0, 0, 1),
  (16, 64, 16, 1, 0, 1),
  (16, 72, 16, 1, 0, 0),
  (16, 80, 16, 0, 0, 0),
  (16, 48, 24, 0, 0, 0),
  (16, 56, 24, 0, 0, 1),
  (16, 64, 24, 0, 0, 1),
  (16, 72, 24, 0, 0, 0),
  (16, 80, 24, 0, 0, 0),
  (16, 48, 32, 0, 0, 0),
  (16, 56, 32, 0, 0, 0),
  (16, 64, 32, 0, 0, 0),
  (16, 72, 32, 0, 0, 0),
  (16, 80, 32, 0, 0, 0);

-- 房间 17: center=(70,20), 布局 4×6
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (17, 46,  8, 0, 0, 0), (17, 54,  8, 0, 0, 0), (17, 62,  8, 1, 0, 0), (17, 70,  8, 1, 0, 0), (17, 78,  8, 0, 0, 0), (17, 86,  8, 0, 0, 0),
  (17, 46, 16, 0, 0, 0), (17, 54, 16, 0, 0, 1), (17, 62, 16, 1, 0, 1), (17, 70, 16, 1, 0, 0), (17, 78, 16, 0, 0, 0), (17, 86, 16, 0, 0, 0),
  (17, 46, 24, 0, 0, 0), (17, 54, 24, 0, 0, 1), (17, 62, 24, 0, 0, 1), (17, 70, 24, 0, 0, 0), (17, 78, 24, 0, 0, 0), (17, 86, 24, 0, 0, 0),
  (17, 46, 32, 0, 0, 0), (17, 54, 32, 0, 0, 0), (17, 62, 32, 0, 0, 0), (17, 70, 32, 0, 0, 0), (17, 78, 32, 0, 0, 0), (17, 86, 32, 0, 0, 0);

-- 房间 18: center=(80,20), 布局 3×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (18, 68,  8, 0, 0, 0), (18, 76,  8, 0, 0, 0), (18, 84,  8, 1, 0, 0), (18, 92,  8, 1, 0, 0), (18,100,  8, 0, 0, 0),
  (18, 68, 16, 0, 0, 0), (18, 76, 16, 0, 0, 1), (18, 84, 16, 1, 0, 1), (18, 92, 16, 1, 0, 0), (18,100, 16, 0, 0, 0),
  (18, 68, 24, 0, 0, 0), (18, 76, 24, 0, 0, 1), (18, 84, 24, 0, 0, 1), (18, 92, 24, 0, 0, 0), (18,100, 24, 0, 0, 0);

-- 房间 19: center=(90,20), 布局 6×3
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (19, 82,  8, 0, 0, 0), (19, 90,  8, 0, 0, 0), (19, 98,  8, 0, 1, 0),
  (19, 82, 16, 0, 0, 0), (19, 90, 16, 0, 0, 1), (19, 98, 16, 0, 0, 0),
  (19, 82, 24, 0, 0, 0), (19, 90, 24, 0, 0, 1), (19, 98, 24, 0, 0, 0),
  (19, 82, 32, 0, 0, 0), (19, 90, 32, 0, 0, 0), (19, 98, 32, 0, 0, 0),
  (19, 82, 40, 0, 0, 0), (19, 90, 40, 0, 0, 0), (19, 98, 40, 0, 0, 0),
  (19, 82, 48, 0, 0, 0), (19, 90, 48, 0, 0, 0), (19, 98, 48, 0, 0, 0);

-- 房间 20: center=(100,20), 布局 4×4
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (20, 88,  6, 0, 0, 0), (20, 96,  6, 0, 0, 0), (20,104,  6, 0, 1, 0), (20,112,  6, 0, 0, 0),
  (20, 88, 14, 0, 0, 0), (20, 96, 14, 1, 1, 1), (20,104, 14, 1, 0, 1), (20,112, 14, 0, 0, 0),
  (20, 88, 22, 0, 0, 0), (20, 96, 22, 0, 0, 1), (20,104, 22, 0, 0, 1), (20,112, 22, 0, 0, 0),
  (20, 88, 30, 0, 0, 0), (20, 96, 30, 0, 0, 0), (20,104, 30, 0, 0, 0), (20,112, 30, 0, 0, 0);
-- 房间 21: center=(10,50), 布局 4×6
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (21, -10, 38, 0, 0, 0),
  (21,  -2, 38, 0, 0, 0),
  (21,   6, 38, 0, 0, 0),
  (21,  14, 38, 0, 0, 0),
  (21,  22, 38, 0, 0, 0),
  (21,  30, 38, 0, 0, 0),
  (21, -10, 46, 0, 0, 0),
  (21,  -2, 46, 0, 0, 0),
  (21,   6, 46, 1, 1, 1),
  (21,  14, 46, 1, 1, 1),
  (21,  22, 46, 0, 0, 0),
  (21,  30, 46, 0, 0, 0),
  (21, -10, 54, 0, 0, 0),
  (21,  -2, 54, 0, 0, 0),
  (21,   6, 54, 0, 0, 0),
  (21,  14, 54, 0, 0, 0),
  (21,  22, 54, 0, 0, 0),
  (21,  30, 54, 0, 0, 0);

-- 房间 22: center=(20,50), 布局 4×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (22,  12, 42, 0, 0, 0),
  (22,  20, 42, 0, 0, 0),
  (22,  28, 42, 1, 0, 0),
  (22,  36, 42, 1, 0, 0),
  (22,  44, 42, 0, 0, 0),
  (22,  12, 50, 1, 0, 0),
  (22,  20, 50, 1, 1, 1),
  (22,  28, 50, 1, 0, 1),
  (22,  36, 50, 1, 0, 0),
  (22,  44, 50, 0, 0, 0),
  (22,  12, 58, 0, 0, 0),
  (22,  20, 58, 0, 0, 0),
  (22,  28, 58, 0, 0, 1),
  (22,  36, 58, 0, 0, 0),
  (22,  44, 58, 0, 0, 0);

-- 房间 23: center=(30,50), 布局 5×3
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (23,  18, 38, 0, 0, 0),
  (23,  30, 38, 0, 0, 0),
  (23,  42, 38, 1, 0, 0),
  (23,  18, 46, 1, 0, 0),
  (23,  30, 46, 1, 0, 1),
  (23,  42, 46, 1, 0, 0),
  (23,  18, 54, 0, 0, 0),
  (23,  30, 54, 0, 0, 1),
  (23,  42, 54, 0, 0, 0),
  (23,  18, 62, 0, 0, 0),
  (23,  30, 62, 0, 0, 0),
  (23,  42, 62, 0, 0, 0),
  (23,  18, 70, 0, 0, 0),
  (23,  30, 70, 0, 0, 0),
  (23,  42, 70, 0, 0, 0);

-- 房间 24: center=(40,50), 布局 3×4
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (24,  32,  42, 0, 0, 0),
  (24,  40,  42, 0, 0, 0),
  (24,  48,  42, 1, 0, 0),
  (24,  56,  42, 1, 0, 0),
  (24,  32,  50, 0, 0, 0),
  (24,  40,  50, 0, 1, 1),
  (24,  48,  50, 1, 0, 1),
  (24,  56,  50, 0, 0, 0),
  (24,  32,  58, 0, 0, 0),
  (24,  40,  58, 0, 0, 1),
  (24,  48,  58, 0, 0, 1),
  (24,  56,  58, 0, 0, 0);

-- 房间 25: center=(50,50), 布局 6×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (25,  26,  34, 0, 0, 0),
  (25,  34,  34, 0, 0, 0),
  (25,  42,  34, 1, 0, 0),
  (25,  50,  34, 1, 0, 0),
  (25,  58,  34, 0, 0, 0),
  (25,  66,  34, 0, 0, 0),
  (25,  26,  42, 0, 0, 0),
  (25,  34,  42, 0, 0, 1),
  (25,  42,  42, 1, 0, 1),
  (25,  50,  42, 1, 1, 1),
  (25,  58,  42, 1, 0, 0),
  (25,  66,  42, 0, 0, 0),
  (25,  26,  50, 0, 0, 0),
  (25,  34,  50, 0, 0, 1),
  (25,  42,  50, 1, 0, 1),
  (25,  50,  50, 1, 0, 1),
  (25,  58,  50, 1, 0, 0),
  (25,  66,  50, 0, 0, 0),
  (25,  26,  58, 0, 0, 0),
  (25,  34,  58, 0, 0, 1),
  (25,  42,  58, 1, 0, 1),
  (25,  50,  58, 1, 0, 0),
  (25,  58,  58, 0, 0, 0),
  (25,  66,  58, 0, 0, 0),
  (25,  26,  66, 0, 0, 0),
  (25,  34,  66, 0, 0, 0),
  (25,  42,  66, 0, 0, 0),
  (25,  50,  66, 0, 0, 0),
  (25,  58,  66, 0, 0, 0),
  (25,  66,  66, 0, 0, 0);

-- 房间 26: center=(10,60), 布局 4×4
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (26,   2,  52, 0, 0, 0),
  (26,  10,  52, 0, 0, 0),
  (26,  18,  52, 1, 0, 0),
  (26,  26,  52, 1, 0, 0),
  (26,   2,  60, 0, 0, 0),
  (26,  10,  60, 0, 1, 1),
  (26,  18,  60, 0, 0, 1),
  (26,  26,  60, 0, 0, 0),
  (26,   2,  68, 0, 0, 0),
  (26,  10,  68, 0, 0, 1),
  (26,  18,  68, 0, 0, 0),
  (26,  26,  68, 0, 0, 0),
  (26,   2,  76, 0, 0, 0),
  (26,  10,  76, 0, 0, 0),
  (26,  18,  76, 0, 0, 0),
  (26,  26,  76, 0, 0, 0);

-- 房间 27: center=(20,60), 布局 3×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (27,  12,  52, 0, 0, 0), (27,  20,  52, 0, 0, 0), (27,  28,  52, 1, 0, 0), (27,  36,  52, 1, 0, 0), (27,  44,  52, 0, 0, 0),
  (27,  12,  60, 1, 0, 0), (27,  20,  60, 1, 1, 1), (27,  28,  60, 1, 0, 1), (27,  36,  60, 1, 0, 0), (27,  44,  60, 0, 0, 0),
  (27,  12,  68, 0, 0, 0), (27,  20,  68, 0, 0, 1), (27,  28,  68, 0, 0, 1), (27,  36,  68, 0, 0, 0), (27,  44,  68, 0, 0, 0);

-- 房间 28: center=(30,60), 布局 6×3
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (28,  14,  52, 0, 0, 0), (28,  30,  52, 0, 0, 0), (28,  46,  52, 1, 0, 0),
  (28,  14,  60, 0, 0, 0), (28,  30,  60, 0, 0, 1), (28,  46,  60, 1, 0, 0),
  (28,  14,  68, 0, 0, 0), (28,  30,  68, 0, 0, 1), (28,  46,  68, 0, 0, 0),
  (28,  14,  76, 0, 0, 0), (28,  30,  76, 0, 0, 0), (28,  46,  76, 0, 0, 0),
  (28,  14,  84, 0, 0, 0), (28,  30,  84, 0, 0, 0), (28,  46,  84, 0, 0, 0),
  (28,  14,  92, 0, 0, 0), (28,  30,  92, 0, 0, 0), (28,  46,  92, 0, 0, 0);

-- 房间 29: center=(40,60), 布局 4×4
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (29,  32,  52, 0, 0, 0), (29,  40,  52, 0, 0, 0), (29,  48,  52, 1, 0, 0), (29,  56,  52, 1, 0, 0),
  (29,  32,  60, 0, 0, 0), (29,  40,  60, 0, 0, 1), (29,  48,  60, 1, 0, 1), (29,  56,  60, 0, 0, 0),
  (29,  32,  68, 0, 0, 0), (29,  40,  68, 0, 0, 1), (29,  48,  68, 0, 0, 1), (29,  56,  68, 0, 0, 0),
  (29,  32,  76, 0, 0, 0), (29,  40,  76, 0, 0, 0), (29,  48,  76, 0, 0, 0), (29,  56,  76, 0, 0, 0);

-- 房间 30: center=(50,60), 布局 5×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (30,  42,  52, 0, 0, 0), (30,  50,  52, 0, 0, 0), (30,  58,  52, 0, 0, 0), (30,  66,  52, 0, 0, 0), (30,  74,  52, 0, 0, 0),
  (30,  42,  60, 1, 0, 0), (30,  50,  60, 1, 1, 1), (30,  58,  60, 1, 0, 1), (30,  66,  60, 1, 0, 0), (30,  74,  60, 0, 0, 0),
  (30,  42,  68, 0, 0, 0), (30,  50,  68, 0, 0, 1), (30,  58,  68, 0, 0, 0), (30,  66,  68, 0, 0, 0), (30,  74,  68, 0, 0, 0),
  (30,  42,  76, 0, 0, 0), (30,  50,  76, 0, 0, 0), (30,  58,  76, 0, 0, 0), (30,  66,  76, 0, 0, 0), (30,  74,  76, 0, 0, 0),
  (30,  42,  84, 0, 0, 0), (30,  50,  84, 0, 0, 0), (30,  58,  84, 0, 0, 0), (30,  66,  84, 0, 0, 0), (30,  74,  84, 0, 0, 0);
-- 房间 31: center=(60,50), 布局 4×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (31,  52,  42, 0, 0, 0),
  (31,  60,  42, 0, 0, 0),
  (31,  68,  42, 1, 0, 0),
  (31,  76,  42, 1, 0, 0),
  (31,  84,  42, 0, 0, 0),
  (31,  52,  50, 0, 0, 0),
  (31,  60,  50, 1, 0, 1),
  (31,  68,  50, 1, 1, 1),
  (31,  76,  50, 1, 0, 1),
  (31,  84,  50, 0, 0, 0),
  (31,  52,  58, 0, 0, 0),
  (31,  60,  58, 0, 0, 1),
  (31,  68,  58, 0, 0, 1),
  (31,  76,  58, 0, 0, 0),
  (31,  84,  58, 0, 0, 0);

-- 房间 32: center=(70,50), 布局 3×6
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (32,  62,  42, 0, 0, 0),
  (32,  70,  42, 0, 0, 0),
  (32,  78,  42, 1, 0, 0),
  (32,  86,  42, 1, 0, 0),
  (32,  94,  42, 0, 0, 0),
  (32, 102,  42, 0, 0, 0),
  (32,  62,  50, 1, 0, 0),
  (32,  70,  50, 1, 1, 1),
  (32,  78,  50, 1, 0, 1),
  (32,  86,  50, 1, 0, 0),
  (32,  94,  50, 0, 0, 0),
  (32, 102,  50, 0, 0, 0);

-- 房间 33: center=(80,50), 布局 5×4
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (33,  64,  42, 0, 0, 0),
  (33,  72,  42, 0, 0, 0),
  (33,  80,  42, 0, 1, 0),
  (33,  88,  42, 0, 0, 0),
  (33,  96,  42, 0, 0, 0),
  (33,  64,  50, 1, 0, 0),
  (33,  72,  50, 1, 0, 1),
  (33,  80,  50, 1, 1, 1),
  (33,  88,  50, 1, 0, 1),
  (33,  96,  50, 0, 0, 0),
  (33,  64,  58, 0, 0, 1),
  (33,  72,  58, 0, 0, 1),
  (33,  80,  58, 0, 0, 1),
  (33,  88,  58, 0, 0, 0),
  (33,  96,  58, 0, 0, 0),
  (33,  64,  66, 0, 0, 0),
  (33,  72,  66, 0, 0, 0),
  (33,  80,  66, 0, 0, 0),
  (33,  88,  66, 0, 0, 0),
  (33,  96,  66, 0, 0, 0);

-- 房间 34: center=(90,50), 布局 4×4
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (34,  82,  42, 0, 0, 0),
  (34,  90,  42, 0, 0, 0),
  (34,  98,  42, 1, 0, 0),
  (34, 106,  42, 0, 0, 0),
  (34,  82,  50, 0, 0, 0),
  (34,  90,  50, 0, 1, 1),
  (34,  98,  50, 1, 0, 1),
  (34, 106,  50, 0, 0, 0),
  (34,  82,  58, 0, 0, 0),
  (34,  90,  58, 0, 0, 1),
  (34,  98,  58, 0, 0, 1),
  (34, 106,  58, 0, 0, 0),
  (34,  82,  66, 0, 0, 0),
  (34,  90,  66, 0, 0, 0),
  (34,  98,  66, 0, 0, 0),
  (34, 106,  66, 0, 0, 0);

-- 房间 35: center=(100,50), 布局 3×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (35,  92,  42, 0, 0, 0),
  (35, 100,  42, 0, 0, 0),
  (35, 108,  42, 0, 1, 0),
  (35, 116,  42, 0, 0, 0),
  (35, 124,  42, 0, 0, 0),
  (35,  92,  50, 0, 0, 0),
  (35, 100,  50, 1, 1, 1),
  (35, 108,  50, 1, 0, 1),
  (35, 116,  50, 1, 0, 0),
  (35, 124,  50, 0, 0, 0),
  (35,  92,  58, 0, 0, 0),
  (35, 100,  58, 0, 0, 1),
  (35, 108,  58, 0, 0, 1),
  (35, 116,  58, 0, 0, 0),
  (35, 124,  58, 0, 0, 0);

-- 房间 36: center=(10,80), 布局 4×6
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (36,  -6,  68, 0, 0, 0),
  (36,   2,  68, 0, 0, 0),
  (36,  10,  68, 1, 0, 0),
  (36,  18,  68, 1, 0, 0),
  (36,  26,  68, 0, 0, 0),
  (36,  34,  68, 0, 0, 0),
  (36,  -6,  76, 0, 0, 0),
  (36,   2,  76, 0, 0, 0),
  (36,  10,  76, 0, 0, 1),
  (36,  18,  76, 0, 0, 0),
  (36,  26,  76, 0, 0, 0),
  (36,  34,  76, 0, 0, 0),
  (36,  -6,  84, 0, 0, 0),
  (36,   2,  84, 0, 0, 1),
  (36,  10,  84, 0, 0, 1),
  (36,  18,  84, 0, 0, 0),
  (36,  26,  84, 0, 0, 0),
  (36,  34,  84, 0, 0, 0);

-- 房间 37: center=(20,80), 布局 5×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (37,  12,  72, 0, 0, 0),
  (37,  20,  72, 0, 0, 0),
  (37,  28,  72, 1, 0, 0),
  (37,  36,  72, 1, 0, 0),
  (37,  44,  72, 0, 0, 0),
  (37,  12,  80, 0, 0, 0),
  (37,  20,  80, 1, 1, 1),
  (37,  28,  80, 1, 0, 1),
  (37,  36,  80, 1, 0, 0),
  (37,  44,  80, 0, 0, 0),
  (37,  12,  88, 0, 0, 0),
  (37,  20,  88, 0, 0, 1),
  (37,  28,  88, 0, 0, 1),
  (37,  36,  88, 0, 0, 0),
  (37,  44,  88, 0, 0, 0),
  (37,  12,  96, 0, 0, 0),
  (37,  20,  96, 0, 0, 0),
  (37,  28,  96, 0, 0, 0),
  (37,  36,  96, 0, 0, 0),
  (37,  44,  96, 0, 0, 0);

-- 房间 38: center=(30,80), 布局 3×4
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (38,  22,  72, 0, 0, 0),
  (38,  30,  72, 0, 0, 0),
  (38,  38,  72, 0, 0, 0),
  (38,  46,  72, 0, 0, 0),
  (38,  22,  80, 0, 0, 1),
  (38,  30,  80, 0, 1, 1),
  (38,  38,  80, 1, 0, 1),
  (38,  46,  80, 0, 0, 0),
  (38,  22,  88, 0, 0, 0),
  (38,  30,  88, 0, 0, 1),
  (38,  38,  88, 0, 0, 0),
  (38,  46,  88, 0, 0, 0);

-- 房间 39: center=(40,80), 布局 4×5
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (39,  32,  72, 0, 0, 0),
  (39,  40,  72, 0, 0, 0),
  (39,  48,  72, 1, 0, 0),
  (39,  56,  72, 1, 0, 0),
  (39,  64,  72, 0, 0, 0),
  (39,  32,  80, 0, 0, 1),
  (39,  40,  80, 1, 1, 1),
  (39,  48,  80, 1, 0, 1),
  (39,  56,  80, 1, 0, 0),
  (39,  64,  80, 0, 0, 0),
  (39,  32,  88, 0, 0, 0),
  (39,  40,  88, 0, 0, 1),
  (39,  48,  88, 0, 0, 1),
  (39,  56,  88, 0, 0, 0),
  (39,  64,  88, 0, 0, 0),
  (39,  32,  96, 0, 0, 0),
  (39,  40,  96, 0, 0, 0),
  (39,  48,  96, 0, 0, 0),
  (39,  56,  96, 0, 0, 0),
  (39,  64,  96, 0, 0, 0);

-- 房间 40: center=(50,80), 布局 5×6
INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket) VALUES
  (40,  34,  68, 0, 0, 0), (40,  42,  68, 0, 0, 0), (40,  50,  68, 0, 1, 0), (40,  58,  68, 0, 0, 0), (40,  66,  68, 0, 0, 0), (40,  74,  68, 0, 0, 0),
  (40,  34,  76, 0, 0, 0), (40,  42,  76, 0, 0, 1), (40,  50,  76, 0, 1, 1), (40,  58,  76, 0, 0, 1), (40,  66,  76, 0, 0, 0), (40,  74,  76, 0, 0, 0),
  (40,  34,  84, 0, 0, 0), (40,  42,  84, 0, 0, 1), (40,  50,  84, 0, 0, 1), (40,  58,  84, 0, 0, 0), (40,  66,  84, 0, 0, 0), (40,  74,  84, 0, 0, 0),
  (40,  34,  92, 0, 0, 0), (40,  42,  92, 0, 0, 0), (40,  50,  92, 0, 0, 0), (40,  58,  92, 0, 0, 0), (40,  66,  92, 0, 0, 0), (40,  74,  92, 0, 0, 0),
  (40,  34, 100, 0, 0, 0), (40,  42, 100, 0, 0, 0), (40,  50, 100, 0, 0, 0), (40,  58, 100, 0, 0, 0), (40,  66, 100, 0, 0, 0), (40,  74, 100, 0, 0, 0);
-- -------------------------------------------------------
-- 房间 41–50 手动式“半自动”插入：基于中心坐标和设施布局
-- 直接复制粘贴到 schema.sql 中即可
-- -------------------------------------------------------
DELIMITER //
CREATE PROCEDURE seedSeats41to50()
BEGIN
  DECLARE rid INT;
  DECLARE cx INT;
  DECLARE cy INT;
  DECLARE rows INT;
  DECLARE cols INT;
  DECLARE startX INT;
  DECLARE startY INT;
  DECLARE i INT;
  DECLARE j INT;
  DECLARE sx INT;
  DECLARE sy INT;
  -- 游标遍历房间41–50
  DECLARE cur CURSOR FOR 
    SELECT room_id, x_coord, y_coord 
      FROM study_room 
     WHERE room_id BETWEEN 41 AND 50 
     ORDER BY room_id;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET rid = -1;

  OPEN cur;
  read_loop: LOOP
    FETCH cur INTO rid, cx, cy;
    IF rid = -1 THEN 
      LEAVE read_loop;
    END IF;
    -- 用 room_id 生成“随机”行列数（3–6）
    SET rows = MOD(rid*7,4) + 3;
    SET cols = MOD(rid*11,4) + 3;
    -- 计算网格起点
    SET startX = cx - ((cols-1)*8)/2;
    SET startY = cy - ((rows-1)*8)/2;

    SET i = 0;
    WHILE i < rows DO
      SET j = 0;
      WHILE j < cols DO
        SET sx = startX + j*8;
        SET sy = startY + i*8;
        -- 插入并计算 near_*
        INSERT INTO seat(room_id, x_coord, y_coord, near_window, near_door, near_socket)
        SELECT 
          rid, sx, sy,
          IF(EXISTS(
            SELECT 1 FROM facility f 
             WHERE f.room_id=rid AND f.type='WINDOW'
               AND POW(f.x_coord-sx,2)+POW(f.y_coord-sy,2)<=25
          ),1,0),
          IF(EXISTS(
            SELECT 1 FROM facility f 
             WHERE f.room_id=rid AND f.type='DOOR'
               AND POW(f.x_coord-sx,2)+POW(f.y_coord-sy,2)<=25
          ),1,0),
          IF(EXISTS(
            SELECT 1 FROM facility f 
             WHERE f.room_id=rid AND f.type='SOCKET'
               AND POW(f.x_coord-sx,2)+POW(f.y_coord-sy,2)<=25
          ),1,0);
        SET j = j + 1;
      END WHILE;
      SET i = i + 1;
    END WHILE;
  END LOOP;
  CLOSE cur;
END;
//
DELIMITER ;

-- 调用并删除存储过程
CALL seedSeats41to50();
DROP PROCEDURE seedSeats41to50;
-- ------------------------------------------------------------------
-- 8. 手动预装 usage_record 表（50 条，房间 1–3，2025-05-01 全天 07:00–07:29）
-- ------------------------------------------------------------------
INSERT INTO usage_record(student_id, seat_id, record_date, signed, time_bitmap) VALUES
  ('1001',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1002', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1003', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1004',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1005', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1006', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1007',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1008', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1009', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1010',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1011', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1012', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1013',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1014', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1015', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1016',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1017', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1018', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1019',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1020', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1021', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1022',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1023', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1024', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1025',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1026', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1027', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1028',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1029', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1030', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1031',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1032', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1033', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1034',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1035', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1036', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1037',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1038', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1039', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1040',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1041', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1042', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1043',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1044', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1045', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1046',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1047', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1048', 22, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1049',  1, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1050', 10, '2025-05-01', FALSE, X'FC0000000000000000000000000000000000000000000000');
-- ------------------------------------------------------------------
-- 手动预装 usage_record 表：50 条记录，房间 1–3，2025-06-20 07:00–07:29
-- ------------------------------------------------------------------
INSERT INTO usage_record(student_id, seat_id, record_date, signed, time_bitmap) VALUES
  ('1001',  1, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1002', 11, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1003', 24, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1004',  4, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1005', 14, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1006', 27, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1007',  7, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1008', 17, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1009', 30, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1010',  1, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1011', 20, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1012', 33, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1013',  4, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1014', 11, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1015', 36, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1016',  8, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1017', 18, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1018', 31, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1019',  2, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1020', 12, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1021', 25, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1022',  5, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1023', 14, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1024', 26, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1025',  3, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1026', 13, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1027', 28, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1028',  6, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1029', 16, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1030', 29, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1031',  9, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1032', 18, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1033', 21, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1034',  5, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1035', 15, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1036', 26, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1037',  1, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1038', 11, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1039', 40, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1040',  4, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1041', 14, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1042', 23, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1043',  7, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1044', 17, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1045', 26, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1046',  1, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1047', 20, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1048', 29, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1049',  4, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000'),
  ('1050', 11, '2025-06-20', FALSE, X'FC0000000000000000000000000000000000000000000000');
