package main

import (
	"fmt"
	"log"
	"net/http"

	wh "campus-social/warehouse"
)

func main() {
	store := wh.NewStore()
	h := wh.NewHandler(store)

	mux := http.NewServeMux()

	// CORS
	mux.HandleFunc("/", func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type")
		if r.Method == "OPTIONS" {
			w.WriteHeader(http.StatusOK)
			return
		}
		http.DefaultServeMux.ServeHTTP(w, r)
	})

	// 看板
	mux.HandleFunc("GET /wms/dashboard", cors(h.Dashboard))

	// 入库预约
	mux.HandleFunc("GET /wms/inbound", cors(h.ListInbound))
	mux.HandleFunc("POST /wms/inbound", cors(h.CreateInbound))
	mux.HandleFunc("POST /wms/inbound/{id}/check", cors(h.CheckInbound))
	mux.HandleFunc("POST /wms/inbound/{id}/shelf", cors(h.ShelfInbound))

	// 库位
	mux.HandleFunc("GET /wms/locations", cors(h.ListLocations))

	// 批次库存
	mux.HandleFunc("GET /wms/batches", cors(h.ListBatches))

	// 盘点
	mux.HandleFunc("GET /wms/counts", cors(h.ListCounts))
	mux.HandleFunc("POST /wms/counts/{id}/execute", cors(h.ExecuteCount))

	// 波次拣货
	mux.HandleFunc("GET /wms/pick-waves", cors(h.ListWaves))
	mux.HandleFunc("POST /wms/pick-waves/{id}/start", cors(h.StartPicking))
	mux.HandleFunc("POST /wms/pick-waves/{id}/complete", cors(h.CompletePicking))

	// 出库
	mux.HandleFunc("GET /wms/outbound", cors(h.ListOutbound))
	mux.HandleFunc("POST /wms/outbound/{id}/review", cors(h.ReviewOutbound))
	mux.HandleFunc("POST /wms/outbound/{id}/complete", cors(h.CompleteOutbound))

	// 异常预警
	mux.HandleFunc("GET /wms/alerts", cors(h.ListAlerts))
	mux.HandleFunc("POST /wms/alerts/{id}/ack", cors(h.AckAlert))
	mux.HandleFunc("POST /wms/alerts/{id}/resolve", cors(h.ResolveAlert))

	fmt.Println("WMS 服务启动于 http://localhost:8081")
	log.Fatal(http.ListenAndServe(":8081", corsMux(mux)))
}

func cors(h http.HandlerFunc) http.HandlerFunc {
	return func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type")
		if r.Method == "OPTIONS" {
			w.WriteHeader(http.StatusOK)
			return
		}
		h(w, r)
	}
}

func corsMux(h http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Access-Control-Allow-Origin", "*")
		w.Header().Set("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
		w.Header().Set("Access-Control-Allow-Headers", "Content-Type")
		if r.Method == "OPTIONS" {
			w.WriteHeader(http.StatusOK)
			return
		}
		h.ServeHTTP(w, r)
	})
}
