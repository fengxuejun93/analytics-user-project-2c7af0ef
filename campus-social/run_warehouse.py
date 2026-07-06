"""仓储管理 WMS 启动脚本（从项目根目录运行：python run_warehouse.py）"""

import sys
import os

# 将 warehouse 包的父目录加入 path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from warehouse.app import app

if __name__ == "__main__":
    print("WMS 服务启动于 http://localhost:8081")
    app.run(port=8081, debug=True)
