import React, { useEffect, useState } from "react";

const RequestPage = () => {
  const [items, setItems] = useState([]);
  const [selectedItem, setSelectedItem] = useState("");
  const [targetUrl, setTargetUrl] = useState("");
  // The URL the server is using now. targetUrl is what is typed in the box.
  const [appliedUrl, setAppliedUrl] = useState("");
  const [urlMessage, setUrlMessage] = useState(null);

  const [numRequests, setNumRequests] = useState(50);
  const [numThreads, setNumThreads] = useState(5);

  const [progress, setProgress] = useState(0);
  const [success, setSuccess] = useState(0);
  const [failure, setFailure] = useState(0);
  const [avgTime, setAvgTime] = useState(0);

    // 🔽 Fetch dropdown items
  useEffect(() => {
    fetch("/api/wizOzManagement/getAvailableClients")
      .then((res) => res.json())
      .then((data) => {
        setItems(data);
      })
      .catch((err) => console.error("Error fetching items:", err));
  }, []);

  // 🔽 Fetch target URL
  useEffect(() => {
    fetch("/api/wizOzManagement/getDestinationUrl")
      .then((res) => res.text())
      .then((data) => {
        setTargetUrl(data);
        setAppliedUrl(data);
      })
      .catch((err) => console.error("Error fetching URL:", err));
  }, []);

  const urlChanged = targetUrl !== appliedUrl;

  // Sends the typed URL to the server. Returns true if the server now uses it.
  const applyUrl = async () => {
    try {
      const res = await fetch("/api/wizOzManagement/setDestinationUrl", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ url: targetUrl }),
      });
      if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        setUrlMessage({ ok: false, text: err.message || "The URL was rejected." });
        return false;
      }
      const used = await res.text();
      setTargetUrl(used);
      setAppliedUrl(used);
      setUrlMessage({ ok: true, text: "Target URL applied." });
      return true;
    } catch (e) {
      setUrlMessage({ ok: false, text: "Could not reach the server." });
      return false;
    }
  };

  // Restores the URL the server was started with.
  const resetUrl = async () => {
    try {
      const res = await fetch("/api/wizOzManagement/resetDestinationUrl", { method: "POST" });
      const used = await res.text();
      setTargetUrl(used);
      setAppliedUrl(used);
      setUrlMessage({ ok: true, text: "Target URL reset to the default." });
    } catch (e) {
      setUrlMessage({ ok: false, text: "Could not reach the server." });
    }
  };

const handleSubmit = async () => {
  // Make sure the server uses the URL that is shown before firing
  if (urlChanged && !(await applyUrl())) {
    return;
  }

  // 1..N request IDs
  const queue = Array.from({ length: numRequests }, (_, i) => i + 1);

  // Shuffle the queue randomly
  for (let i = queue.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [queue[i], queue[j]] = [queue[j], queue[i]];
  }

  let completed = 0;
  let successCount = 0;
  let failureCount = 0;
  let totalTime = 0;

  const worker = async () => {
    while (queue.length > 0) {
      const requestId = queue.shift(); // pick a random one (shuffled)

      const start = performance.now();

      try {
        // Option 1: send requestId as query param
        const url = "/api/fireRequest/"+ selectedItem + "/" + requestId;
        const res = await fetch(url);

        if (!res.ok) throw new Error("Failed");

        await res.text();

        successCount++;
        console.log(`✅ Request ${requestId} success`);

      } catch (err) {
        failureCount++;
        console.error(`❌ Request ${requestId} failed`);
      } finally {
        const end = performance.now();
        totalTime += (end - start);

        completed++;

        // Update UI live
        setProgress((completed / numRequests) * 100);
        setSuccess(successCount);
        setFailure(failureCount);
        setAvgTime((totalTime / completed).toFixed(2));
      }
    }
  };

  // Start "threads" (workers)
  const workers = Array.from({ length: numThreads }, () => worker());
  await Promise.all(workers);

  console.log("All requests completed");
};

  return (
    <div style={styles.page}>
      <div style={styles.card}>
        <h2 style={styles.title}>🚀 Load Test Dashboard</h2>

        <div style={styles.row}>
          <div style={styles.field}>
            <label>Item</label>
            <select value={selectedItem} onChange={e => setSelectedItem(e.target.value)} style={styles.input}>
                           <option value="">-- Select --</option>
               {Object.entries(items).map(([key, value]) => (
                <option key={key} value={key}>
                  {value}
                </option>
              ))}
            </select>
          </div>

          <div style={{ ...styles.field, flex: "2", minWidth: "260px" }}>
            <label>Target URL (the number is appended)</label>
            <input
              value={targetUrl}
              onChange={e => { setTargetUrl(e.target.value); setUrlMessage(null); }}
              style={styles.input}
              placeholder="http://host:port/path/"
              spellCheck={false}
            />
            <div style={styles.urlButtons}>
              <button onClick={applyUrl} disabled={!urlChanged} style={styles.smallButton}>Apply</button>
              <button onClick={resetUrl} style={styles.smallButton}>Reset to default</button>
            </div>
            {urlMessage && (
              <div style={{ ...styles.urlMessage, color: urlMessage.ok ? "#1a7f37" : "#c62828" }}>
                {urlMessage.text}
              </div>
            )}
          </div>

          <div style={styles.field}>
            <label>Requests: {numRequests}</label>
            <input type="range" min="1" max="1000" value={numRequests}
              onChange={e => setNumRequests(Number(e.target.value))} />
          </div>

          <div style={styles.field}>
            <label>Threads: {numThreads}</label>
            <input type="range" min="1" max="50" value={numThreads}
              onChange={e => setNumThreads(Number(e.target.value))} />
          </div>

          <div style={styles.field}>
            <button onClick={handleSubmit} style={styles.button}>
              Fire 🚀
            </button>
          </div>
        </div>

        {/* 📊 Stats */}
        <div style={styles.stats}>
          <div>✅ Success: {success}</div>
          <div>❌ Failure: {failure}</div>
          <div>⏱ Avg Time: {avgTime} ms</div>
        </div>

        {/* 📈 Progress Bar */}
        <div style={styles.progressBar}>
          <div style={{ ...styles.progressFill, width: `${progress}%` }} />
        </div>

        <p style={{ textAlign: "center" }}>{progress.toFixed(1)}%</p>
      </div>
    </div>
  );
};

const styles = {
  page: {
    height: "100vh",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    background: "linear-gradient(135deg, #1e3c72, #2a5298)",
    fontFamily: "Arial",
  },
  card: {
    background: "#fff",
    padding: "30px",
    borderRadius: "12px",
    width: "95%",
    maxWidth: "1100px",
  },
  title: {
    textAlign: "center",
    marginBottom: "20px",
  },
  row: {
    display: "flex",
    gap: "15px",
    flexWrap: "wrap",
  },
  field: {
    flex: "1",
    minWidth: "150px",
    display: "flex",
    flexDirection: "column",
  },
  input: {
    padding: "8px",
  },
  button: {
    marginTop: "22px",
    padding: "10px",
    background: "linear-gradient(135deg, #ff7e5f, #feb47b)",
    border: "none",
    color: "#fff",
    cursor: "pointer",
  },
  urlButtons: {
    display: "flex",
    gap: "8px",
    marginTop: "6px",
  },
  smallButton: {
    padding: "4px 10px",
    fontSize: "12px",
    cursor: "pointer",
  },
  urlMessage: {
    marginTop: "4px",
    fontSize: "12px",
  },
  stats: {
    display: "flex",
    justifyContent: "space-around",
    marginTop: "20px",
    fontWeight: "bold",
  },
  progressBar: {
    marginTop: "20px",
    height: "20px",
    background: "#eee",
    borderRadius: "10px",
    overflow: "hidden",
  },
  progressFill: {
    height: "100%",
    background: "linear-gradient(90deg, #00c6ff, #0072ff)",
    transition: "width 0.2s",
  },
};

export default RequestPage;