async function getPdf() {
	const status = document.getElementById('status');

	const basePath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1)) || '';
	fetch(`${basePath}/ApiGateway`, {
		method: 'GET',
		headers: {
			'Content-Type': 'application/pdf',
		}
	})

		.then(response => {
			if (!response.ok) {
				throw new Error('PDFダウンロード失敗: ' + response.status);
			}
			return response.blob();
		})
		.then(blob => {

			const url = window.URL.createObjectURL(blob);
			const a = document.createElement('a');
			a.href = url;
			a.download = '絵で見て分かるJSBase開発.pdf';
			document.body.appendChild(a);
			a.click();

			window.URL.revokeObjectURL(url);
			document.body.removeChild(a);

			status.textContent = 'PDFダウンロードが完了しました。';
		})
		.catch(error => {
			status.textContent = error.message;
		});
}

