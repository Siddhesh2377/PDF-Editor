from fpdf import FPDF

def create_sample_pdf(file_path):
    pdf = FPDF()
    pdf.add_page()
    pdf.set_font("Arial", size=14)
    pdf.cell(200, 10, txt="Hello from Python PDF!", ln=True, align="C")
    pdf.output(file_path)
