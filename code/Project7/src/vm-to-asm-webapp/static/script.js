function convertCode() {
  let vmCode = document.getElementById("vmInput").value;

  fetch("/convert", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ vm_code: vmCode }),
  })
    .then((response) => response.json())
    .then((data) => {
      if (data.error) {
        document.getElementById("asmOutput").innerText = "错误: " + data.error;
      } else {
        document.getElementById("asmOutput").innerText = data.asm_code;
      }
    })
    .catch((error) => {
      document.getElementById("asmOutput").innerText = "请求失败: " + error;
    });
}
