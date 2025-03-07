from flask import Flask, request, jsonify, send_from_directory
import subprocess
import os

app = Flask(__name__)

UPLOAD_FOLDER = "temp_files"
VMT_DIR = "vmt"  # VMT.class 存放的目录
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route('/')
def serve_index():
    return send_from_directory('static', 'index.html')

@app.route('/convert', methods=['POST'])
def convert_vm_to_asm():
    try:
        vm_code = request.json.get("vm_code", "")
        if not vm_code:
            return jsonify({"error": "No VM code provided"}), 400

        vm_path = os.path.join(UPLOAD_FOLDER, "temp.vm")
        asm_path = os.path.join(UPLOAD_FOLDER, "temp.asm")

        with open(vm_path, "w") as f:
            f.write(vm_code)

        process = subprocess.run(["java", "-cp", VMT_DIR, "VMT", vm_path],
                                 stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        if process.returncode != 0:
            return jsonify({"error": process.stderr.decode()}), 500

        if not os.path.exists(asm_path):
            return jsonify({"error": "ASM file was not generated"}), 500

        with open(asm_path, "r") as f:
            asm_code = f.read()

        return jsonify({"asm_code": asm_code})

    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True)