# embedder.py

import numpy as np
import cv2
import os

def dummy_embedding(image_path):
    """
    Generates a dummy embedding vector from an image.
    Replace this logic with a real embedding model like FaceNet or retina-based model.
    """
    image = cv2.imread(image_path, cv2.IMREAD_GRAYSCALE)
    if image is None:
        raise ValueError("Invalid image input or unreadable format.")
    
    resized = cv2.resize(image, (100, 100)).flatten()
    embedding = resized[:128]  # Fake 128-dimensional vector
    return embedding

def save_embedding(user_id, image_path):
    try:
        embedding = dummy_embedding(image_path)
        save_path = f"saved_embeddings/{user_id}.npy"
        np.save(save_path, embedding)
        return True
    except Exception as e:
        print(f"Error saving embedding: {e}")
        return False

def compare_embedding(image_path, user_id):
    try:
        embedding = dummy_embedding(image_path)
        stored_path = f"saved_embeddings/{user_id}.npy"

        if not os.path.exists(stored_path):
            return False

        stored_embedding = np.load(stored_path)
        distance = np.linalg.norm(embedding - stored_embedding)
        return distance < 50  # Dummy threshold
    except Exception as e:
        print(f"Error comparing embeddings: {e}")
        return False
