from flask import Flask, request, jsonify
from embedder import save_embedding, compare_embedding
import os

app = Flask(__name__)

# Ensure the folder for temp uploads exists
os.makedirs("temp_uploads", exist_ok=True)

@app.route('/save-embedding', methods=['POST'])
def save_route():
    try:
        user_id = request.form['user_id']
        file = request.files['image']
        
        # Save image temporarily
        image_path = f"temp_uploads/{user_id}.png"
        file.save(image_path)

        success = save_embedding(user_id, image_path)
        return jsonify({'status': 'success' if success else 'fail'})
    except Exception as e:
        return jsonify({'status': 'error', 'message': str(e)})

@app.route('/match-embedding', methods=['POST'])
def match_route():
    try:
        user_id = request.form['user_id']
        file = request.files['image']

        # Save test image temporarily
        image_path = f"temp_uploads/{user_id}_temp.png"
        file.save(image_path)

        match = compare_embedding(image_path, user_id)
        return jsonify({'matched': match})
    except Exception as e:
        return jsonify({'matched': False, 'error': str(e)})

if __name__ == '__main__':
    app.run(port=5001, debug=True)
